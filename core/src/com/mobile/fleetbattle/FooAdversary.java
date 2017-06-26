package com.mobile.fleetbattle;

/**
 * Created by Facoch on 26/06/17.
 * expects numbers from 1 to 10 for ships, 0= water, 100 = hit water, 101 -110 hit ship
 */

public class FooAdversary extends Adversary {
    private int[][] matrice = {{1,2,3,4,5,5,6,6,7,7},{8,8,8,9,9,9,0,0,0,0},{0,0,0,0,0,0,0,0,0,0},
            {0,0,0,0,0,0,0,0,0,0},{0,0,0,0,0,0,0,0,0,0},{0,0,0,0,0,0,0,0,0,0},{0,0,0,0,0,0,0,0,0,10},
            {0,0,0,0,0,0,0,0,0,10},{0,0,0,0,0,0,0,0,0,10},{0,0,0,0,0,0,0,0,0,10}};

    public boolean hit(int y, int x){
        if(matrice[y][x]!=0 && matrice[y][x]<100) {
            matrice[y][x]+=100;
            return true;
        }
        return false;
    }

    public Ship destroyed(int y, int x){
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

    public boolean lost(){
        for (int i = 0; i < 10; i++) {
            for (int j = 0; j < 10; j++) {
                if(matrice[i][j]>0 & matrice[i][j]<100){
                    return false;
                }
            }
        }
        return true;
    }
}
