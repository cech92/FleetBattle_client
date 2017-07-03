package com.mobile.fleetbattle;

import com.badlogic.gdx.math.Rectangle;

import org.w3c.dom.css.Rect;

import java.util.Random;

/**
 * Created by Facoch on 03/07/17.
 * Utility class to create random dispositions both for the adversary and the player.
 */

class RandomDisposition {
    int[][] dispositionMatrix;

    RandomDisposition(){
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

    Rectangle returnShip(int name){
        int i=0; int j=0;
        while (dispositionMatrix[i][j]!=name){
            if(j<9){
                j++;
            }else{
                if(i<9){
                    j=0; i++;
                }else {//if called on a non existing ship will return an empty rectangle
                    new Rectangle(0,0,0,0);
                }
            }
        }
        int x= 80 + 80*j; int y= 80 + 80*i;
        int heigth = 80; int width= 80;
        if(i<9){
            while(i<9 && dispositionMatrix[i][j]==dispositionMatrix[i+1][j]){
                heigth += 80;
                i++;
            }
        }
        if(j<9){
            while(j<9 && dispositionMatrix[i][j]==dispositionMatrix[i][j+1]){
                width += 80;
                j++;
            }
        }
        return new Rectangle(x,y,width,heigth);
    }
}
