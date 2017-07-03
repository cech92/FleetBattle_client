package com.mobile.fleetbattle;

import java.util.Random;

import java.util.Vector;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

/**
 * Created by Facoch on 03/07/17.
 *
 * uses a disposition matrix that expects numbers from 1 to 10 for ships,
 * 0= water, 100 = hit water, [101,110] hit ship
 *
 * This Ai goes randomly until it hits something, then gives preference to the squares near the hit
 * one. When a ship is sank those preferences are cleared.
 * This ai could be made better by considering the "direction" of the hit squares. It also assumes that
 * all the preferences involved only one ship, so when a ship is sank all preferences are deleted, but
 * if two ships are close this cuold be not true.
 */

class ComputerAdversary extends Adversary {

    private final ExecutorService pool = Executors.newFixedThreadPool(10);

    private boolean[][] alreadyAttacked;
    private int[][] dispositionMatrix;
    private Vector<Coord> plausibleAttacks;
    private Coord lastAttack;

    private class Coord{
        int x;
        int y;
        Coord(int newX, int newY){
            x=newX; y=newY;
        }
    }

    ComputerAdversary(){
        // default positioning is random
        dispositionMatrix = new int[10][10];
        dispositionMatrix = new RandomDisposition().dispositionMatrix;
        plausibleAttacks = new Vector<Coord>();
        alreadyAttacked = new boolean[10][10];
    }



    //used to attack the adversary. should return three boolean values, if the coordinate was an hit, if a ship sank, if the adversary has lost.
    public Future<Results> attack(final int y, final int x){
        return pool.submit(new Callable<Results>() {
            @Override
            public Results call(){
                return new Results(hit(y,x),sank(y,x),lost());
            }
        });

    }


    //returns the coordinates attacked by the enemy:  ship is used only for the y, x coordinates (size and up should always be 0)
    public Future<Ship> getAttack(){
        return pool.submit(new Callable<Ship>() {
            @Override
            public Ship call(){
                Random rand = new Random();

                int pendingAttacks= plausibleAttacks.size();
                if (pendingAttacks!=0){
                    int i = rand.nextInt(pendingAttacks);
                    Coord co = plausibleAttacks.get(i);
                    plausibleAttacks.remove(i);
                    alreadyAttacked[co.y][co.x]=true;
                    lastAttack= new Coord(co.x,co.y);
                    return new Ship(co.y,co.x,0,0);
                }else{
                    int x = rand.nextInt(10);
                    int y = rand.nextInt(10);
                    while (alreadyAttacked[y][x]){ // we need to find an available cell
                        int prob = rand.nextInt(10);
                        if(prob==0) {//10% probability to search the first next available cell
                            if (x < 9) {
                                x++;
                            } else {
                                if (y < 9) {
                                    x = 0;
                                    y++;
                                } else {
                                    x = 0;
                                    y = 0;
                                }
                            }
                        }else{ //otherwise just check another cell randomly
                            x = rand.nextInt(10);
                            y = rand.nextInt(10);
                        }
                    }
                    alreadyAttacked[y][x]=true;
                    lastAttack= new Coord(x,y);
                    return new Ship(y,x,0,0);
                }
            }
        });
    }

    //used to respond to an adversary attack
    public void giveResults(Results res){
        if(res.hit){
            if(lastAttack.y<9){
                if (!alreadyAttacked[lastAttack.y+1][lastAttack.x])
                    plausibleAttacks.add(new Coord(lastAttack.x,lastAttack.y+1));
            }
            if(lastAttack.y>0){
                if (!alreadyAttacked[lastAttack.y-1][lastAttack.x])
                    plausibleAttacks.add(new Coord(lastAttack.x,lastAttack.y-1));
            }
            if(lastAttack.x<9){
                if (!alreadyAttacked[lastAttack.y][lastAttack.x+1])
                    plausibleAttacks.add(new Coord(lastAttack.x+1,lastAttack.y));
            }
            if(lastAttack.y>0){
                if (!alreadyAttacked[lastAttack.y][lastAttack.x-1])
                    plausibleAttacks.add(new Coord(lastAttack.x-1,lastAttack.y));
            }

        }
        if(res.sank.size!=0){
            // I sank a ship, the plausible coordinates are not plausible anymore
            plausibleAttacks.clear();
        }
    }


    private boolean hit(int y, int x){
        if(dispositionMatrix[y][x]!=0 && dispositionMatrix[y][x]<100) {
            dispositionMatrix[y][x]+=100;
            return true;
        }
        return false;
    }


    private Ship sank(int a, int b){
        int y=a;
        int x=b;
        int num = dispositionMatrix[y][x];
        int up = 0;
        int size= 0;
        if (num<101) {
            return new Ship(0, 0, 0, 0);
        }else{
            for (int i = 0; i < 10; i++) { // i is the y coordinate
                for (int j = 0; j < 10; j++) { // j is the x coordinate
                    if(dispositionMatrix[i][j]==num-100){
                        return new Ship(0, 0, 0, 0);
                    }
                    if(dispositionMatrix[i][j]==num){
                        size++; //valid only if ship sank
                    }
                }
            }
        }
        if(y<9){
            if(num==dispositionMatrix[y+1][x]){
                up=1;
            }
        }
        if(y>0){
            while(y>0 && num==dispositionMatrix[y-1][x]){
                --y;
                up=1;
            }
        }
        if(x>0){
            while(x>0 && num==dispositionMatrix[y][x-1]){
                --x;
            }
        }
        return new Ship(y, x, size, up);
    }

    private boolean lost(){
        for (int i = 0; i < 10; i++) {
            for (int j = 0; j < 10; j++) {
                if(dispositionMatrix[i][j]>0 & dispositionMatrix[i][j]<100){
                    return false;
                }
            }
        }
        return true;
    }

}
