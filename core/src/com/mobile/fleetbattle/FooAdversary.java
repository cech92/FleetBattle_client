package com.mobile.fleetbattle;

import java.util.Random;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

/**
 * Created by Facoch on 26/06/17.
 * expects numbers from 1 to 10 for ships, 0= water, 100 = hit water, 101 -110 hit ship
 */

class FooAdversary extends Adversary {
    private final ExecutorService pool = Executors.newFixedThreadPool(10);

    private int attackX=0;
    private int attackY=0;

    private int[][] dispositionMatrix;
    
    FooAdversary(){
        createDisposition();
    }

    private boolean hit(int y, int x){
        if(dispositionMatrix[y][x]!=0 && dispositionMatrix[y][x]<100) {
            dispositionMatrix[y][x]+=100;
            return true;
        }
        return false;
    }

    private void createDisposition(){
        dispositionMatrix=new int[10][10];
        placeBoats(1,4,10);
        placeBoats(2,3,9);
        placeBoats(3,2,7);
        placeBoats(4,1,4);
    }
    
    private void placeBoats(int numBoats, int dimBoats, int name){
        Random generator = new Random();
        int y; int x; boolean up;
        for (int k = 0; k < numBoats; k++) {
            y = generator.nextInt(10);
            x = generator.nextInt(10);
            up = generator.nextBoolean();
            while(up && y>10-dimBoats){
                y = generator.nextInt(10);
            }
            while(!up && x>10-dimBoats){
                x = generator.nextInt(10);
            }
            int i=0;
            if(up){
                while(i<dimBoats){
                    if(dispositionMatrix[y+i][x]==0){
                        dispositionMatrix[y+i][x]=name;
                        i++;
                    }else{
                        for (int j = 0; j < i; j++) {
                            dispositionMatrix[y+j][x]=0;
                        }
                        i=0;
                        y = generator.nextInt(10);
                        x = generator.nextInt(10);
                        while(y>7){
                            y = generator.nextInt(10);
                        }
                    }
                }
            }else{
                while(i<dimBoats){
                    if(dispositionMatrix[y][x+i]==0){
                        dispositionMatrix[y][x+i]=name;
                        i++;
                    }else{
                        for (int j = 0; j < i; j++) {
                            dispositionMatrix[y][x+j]=0;
                        }
                        i=0;
                        y = generator.nextInt(10);
                        x = generator.nextInt(10);
                        while(x>7){
                            x = generator.nextInt(10);
                        }
                    }
                }
            }
            name = name-1;
        }
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

    //used to attack the adversary. should return three boolean values, if the coordinate was an hit, if a ship sank, if the adversary has lost.
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

    }


    //returns the coordinates attacked by the enemy:  ship is used only for the y, x coordinates (size and up should always be 0)
    public Future<Ship> getAttack(){
        return pool.submit(new Callable<Ship>() {
            @Override
            public Ship call(){
                int x= attackX;
                int y= attackY;
                if (attackX!=9){attackX++;}else{attackX=0;attackY++;}
                try {
                    Thread.sleep(500);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                return new Ship(y,x,0,0);
            }
        });
    }

    //used to respond to an adversary attack
    public void giveResults(Results res){

    }

}
