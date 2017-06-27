package com.mobile.fleetbattle;

import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import javax.xml.transform.Result;

/**
 * Created by Facoch on 26/06/17.
 * expects numbers from 1 to 10 for ships, 0= water, 100 = hit water, 101 -110 hit ship
 */

public class FooAdversary extends Adversary {
    private final ExecutorService pool = Executors.newFixedThreadPool(10);

    private int attackX=0;
    private int attackY=0;

    private int[][] matrice = {{1,2,3,4,5,5,6,6,7,7},{8,8,8,9,9,9,0,0,0,0},{0,0,0,0,0,0,0,0,0,0},
            {0,0,0,0,0,0,0,0,0,0},{0,0,0,0,0,0,0,0,0,0},{0,0,0,0,0,0,0,0,0,0},{0,0,0,0,0,0,0,0,0,10},
            {0,0,0,0,0,0,0,0,0,10},{0,0,0,0,0,0,0,0,0,10},{0,0,0,0,0,0,0,0,0,10}};

    private boolean hit(int y, int x){
        if(matrice[y][x]!=0 && matrice[y][x]<100) {
            matrice[y][x]+=100;
            return true;
        }
        return false;
    }

    public Ship sank(int a, int b){
        int y=a;
        int x=b;
        int num = matrice[y][x];
        int up = 0;
        int size= 0;
        if (num<101) {
            return new Ship(0, 0, 0, 0);
        }else{
            for (int i = 0; i < 10; i++) { // i is the y coordinate
                for (int j = 0; j < 10; j++) { // j is the x coordinate
                    if(matrice[i][j]==num-100){
                        return new Ship(0, 0, 0, 0);
                    }
                    if(matrice[i][j]==num){
                        size++; //valid only if ship sank
                    }
                }
            }
        }
        if(y<9){
            if(num==matrice[y+1][x]){
                up=1;
            }
        }
        if(y>0){
            while(y>0 && num==matrice[y-1][x]){
                --y;
                up=1;
            }
        }
        if(x>0){
            while(x>0 && num==matrice[y][x-1]){
                --x;
            }
        }
        return new Ship(y, x, size, up);
    }

    private boolean lost(){
        for (int i = 0; i < 10; i++) {
            for (int j = 0; j < 10; j++) {
                if(matrice[i][j]>0 & matrice[i][j]<100){
                    return false;
                }
            }
        }
        return true;
    }

    //used to attack the adversary. should return three booelan values, if the coordinate was an hit, if a ship sank, if the adversary has lost.
    public Future<Results> attack(final int y, final int x){
        return pool.submit(new Callable<Results>() {
            @Override
            public Results call(){
                try {
                    Thread.sleep(500);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                return new Results(hit(y,x),sank(y,x),lost());
            }
        });

    };


    //returns the coordinates attacked by the enemy:  ship is used only for the y, x coordinates (size and up should always be 0)
    public Future<Ship> getAttack(){
        return pool.submit(new Callable<Ship>() {
            @Override
            public Ship call(){
                int x= attackX;
                int y= attackY;
                if (attackX!=9){attackX++;}else{attackX=0;attackY++;};
                try {
                    Thread.sleep(500);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                return new Ship(y,x,0,0);
            }
        });
    };

    //used to respond to an adversary attack
    public void giveResults(Results res){

    };

}
