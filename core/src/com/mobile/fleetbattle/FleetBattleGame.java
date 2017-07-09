package com.mobile.fleetbattle;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.InputProcessor;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.Array;

import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.utils.viewport.StretchViewport;

import java.util.concurrent.Future;

import static java.lang.Math.abs;

class FleetBattleGame extends ApplicationAdapter implements InputProcessor{
	private Adversary enemy;

	private Texture targetImage;
	private Texture fireImage;
	private Texture waterImage;
	private Texture portaImage;
	private Texture incrImage;
	private Texture torpImage;
	private Texture subImage;
	private Texture gridImage;
	private Texture portaImageRot;
	private Texture incrImageRot;
	private Texture torpImageRot;
	private Texture winImage;
	private Texture loseImage;

	private BitmapFont font;
	private String messageString = "";

	private OrthographicCamera camera;
	private SpriteBatch batch;
	private Vector3 touchPos;
	private Stage stage;

	private Array<Rectangle> ships;
	private Array<Rectangle> misses;
	private Array<Rectangle> hits;

	private Rectangle target;
	private boolean targetShown = false;
	private Coord targetCo;


	private boolean locked = false;
	private int lockedTime = 0;
	private int activeSub = 0;

	//state variable
	private static boolean gameRunning=false;
	private int state=0;
	/*
	* State 0: Not disposed yet. When disposed go to state 12
	* -- Player turn --
	* state 1: wait for user attack
	* state 2: animating attack
	* state 3: wait for enemy response
	* state 4: hit -> animate and return to state 1 (or goto 13 if won)
	* state 5: miss -> animate
	* state 6: end turn, animate screen scroll
	* -- Enemy turn --
	* state 7: wait for enemy attack
	* state 8: animate attack
	* state 9: send response
	* state 10: hit -> animate and return to state 7 (or goto 14 id lost)
	* state 11: miss -> animate
	* state 12: start turn, animate screen scroll, return to state 1
	*
	* state 13: Player has won
	* state 14: Player has lost
	*/

	private int wait=0;
	@SuppressWarnings("FieldCanBeLocal")
	private final int waitingTime=60; //60 frames = 1 sec
	private	boolean secondHit=false;

	private int[][] disposizione;
	private boolean[][] avversarioToccato;


	private class Coord{
		int x;
		int y;
		Coord(int a, int b){
			x=a; y=b;
			x=a; y=b;
		}
	}

	FleetBattleGame(com.mobile.fleetbattle.Adversary en){
		enemy = en;
	}

	@Override
	public void create () {
		state = 0;
		Skin skin = new Skin(Gdx.files.internal("Holo-dark-xhdpi.json"));
		font = new BitmapFont(Gdx.files.internal("Roboto-xhdpi.fnt"),Gdx.files.internal("Roboto-xhdpi.png"),false);

		disposizione = new int[10][10];
		avversarioToccato = new boolean[10][10];

		targetImage = new Texture(Gdx.files.internal("target.png"));
		fireImage = new Texture(Gdx.files.internal("fire.png"));
		waterImage = new Texture(Gdx.files.internal("water.png"));
		portaImage = new Texture(Gdx.files.internal("porta.png"));
		incrImage = new Texture(Gdx.files.internal("incr.png"));
		torpImage = new Texture(Gdx.files.internal("torp.png"));
		subImage = new Texture(Gdx.files.internal("sub.png"));
		gridImage = new Texture(Gdx.files.internal("grid.png"));
		portaImageRot = new Texture(Gdx.files.internal("portarot.png"));
		incrImageRot = new Texture(Gdx.files.internal("incrrot.png"));
		torpImageRot = new Texture(Gdx.files.internal("torprot.png"));
		winImage = new Texture(Gdx.files.internal("win.png"));
		loseImage = new Texture(Gdx.files.internal("lose.png"));

		camera = new OrthographicCamera();
		camera.setToOrtho(false, 920, 1120);
		touchPos = new Vector3();

		batch = new SpriteBatch();
		StretchViewport viewp = new StretchViewport(920, 1120, camera);
		stage = new Stage(viewp,batch);


		misses = new Array<Rectangle>();
		hits = new Array<Rectangle>();
		ships = new Array<Rectangle>();

		// Random disposition for the ships
		RandomDisposition disp = new RandomDisposition();
		for (int i = 1; i < 11; i++) {
			Rectangle aux = disp.returnShip(i);
			ships.add(aux);
		}

		final TextButton button = new TextButton("Start Battle!", skin, "default");
		button.setWidth(820);
		button.setHeight(130);
		button.setPosition(50, 970);
		button.getLabel().setFontScale(2);

		button.addListener(new ClickListener(){
			@Override
			public void clicked(InputEvent event, float x, float y){
				int errori = controllaErrori();
				switch (errori){
					case 0 :
						computeMatrix();
						gameRunning=true;
						state=12;
						break;
					case 1 : button.getLabel().setFontScale(1);
							button.getLabel().setColor(1,0.25f,0,1);
							button.setText("Invalid Disposition:\n Change ships outside the grid");
							break;
					case 2 : button.getLabel().setFontScale(1);
						button.getLabel().setColor(1,0.25f,0,1);
						button.setText("Invalid Disposition:\n Change overlapping ships");
						break;
					case 3 : button.getLabel().setFontScale(1);
						button.getLabel().setColor(1,0.25f,0,1);
						button.setText("Invalid Disposition:\n Change overlapping or outside the grid ships");
						break;
				}
			}
		});
		stage.addActor(button);

		Gdx.input.setInputProcessor(stage);
	}

	@Override
	public void render () {
		Gdx.gl.glClearColor(1, 1, 0.8f, 1);
		Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

		camera.update();

		if(state>0){
			//enemy's turn
			if(state==7){
				if(secondHit && wait<waitingTime/2){
					wait++;
				}else {
					Future<Ship> futAttack = enemy.getAttack();
					Ship attack;
					try {
						attack = futAttack.get();
					} catch (Exception ex) {
						attack = new Ship(0, 0, 0, 0);
					}
					wait=0;
					state=8;
					targetCo = new Coord(attack.x, attack.y);
				}

			}
			if(state==9) {
				int x = targetCo.x;
				int y = targetCo.y;
				boolean hit = hit(y,x);
				boolean lost = lost();
				Results res = new Results(hit,sank(y,x),lost);
				enemy.giveResults(res);

				if(hit){
					secondHit=true;
					hits.add(new Rectangle((x * 80) + 80 , (y * 80) + 80, 80, 80));
					if(lost){
						messageString ="Click back to exit game.";
						state = 14;
						gameRunning = false;
					}else {
						messageString ="Hit! The enemy gets another attack.";
						state = 10;
					}
				}else{
					messageString = "Miss! It's your turn now.";
					secondHit=false;
					misses.add(new Rectangle((x * 80) + 80 , (y * 80) + 80, 80, 80));
					state=11;
				}

			}
			// Transitions between turns
			if(state==12) {
				if(wait<waitingTime){
					wait=wait+1;
				}else {
					if (camera.position.x < 920 + camera.viewportWidth / 2) {
						camera.translate(+40, 0, 0);
					} else {
						messageString = "Select a cell to attack";
						wait=0;
						state=1;
					}
					camera.update();
				}
			}
			if(state==6) {
				if(wait<waitingTime){
					wait=wait+1;
				}else {
					if (camera.position.x > camera.viewportWidth / 2) {
						camera.translate(-40, 0, 0);
					} else {
						messageString = "Please wait for the enemy attack.";
						wait=0;
						state=7;
					}
					camera.update();
				}
			}

			//Draw everything

			batch.setProjectionMatrix(camera.combined);
			batch.begin();
			batch.draw(gridImage, 0, 0);
			batch.draw(gridImage, 920, 0);
			for (Rectangle sub:ships) {
				switch ((int)sub.width){
					case 80 : switch ((int) sub.height){
						case 80 : batch.draw(subImage, sub.x, sub.y, sub.width, sub.height); break;
						case 160 : batch.draw(torpImageRot, sub.x, sub.y, sub.width, sub.height); break;
						case 240 : batch.draw(incrImageRot, sub.x, sub.y, sub.width, sub.height); break;
						case 320 : batch.draw(portaImageRot, sub.x, sub.y, sub.width, sub.height); break;
					}  break;
					case 160 : 	batch.draw(torpImage, sub.x, sub.y, sub.width, sub.height); break;
					case 240 : 	batch.draw(incrImage, sub.x, sub.y, sub.width, sub.height); break;
					case 320 : 	batch.draw(portaImage, sub.x, sub.y, sub.width, sub.height); break;
				}
			}
			if(targetShown){
				batch.draw(targetImage, target.x, target.y, target.width,target.height);
			}
			for (Rectangle miss:misses) {
				batch.draw(waterImage, miss.x, miss.y, 80, 80);
			}
			for (Rectangle hit:hits) {
				batch.draw(fireImage, hit.x, hit.y, 80, 80);
			}
			if(state==14){
				batch.draw(loseImage,80, 260);
			}
			if(state==13){
				batch.draw(winImage,1000, 260);
			}

			font.setColor(0,0.2f,1,1);
			font.getData().setScale(1.3f,1.5f);
			font.draw(batch, messageString, 30, 1040, 860, Align.center, true);
			font.draw(batch, messageString, 950, 1040, 860, Align.center, true);

			batch.end();

			if(state==2||state==8){
				if(state==2){
					messageString="Please wait for the enemy's response.";
				}
				if(!targetShown){
					if(state==2){
						target = new Rectangle((1000 + targetCo.x * 80) - 720, (80 + 80 * targetCo.y) - 720, 1520, 1520);
					}else{ //state==8
						target = new Rectangle((80 + targetCo.x * 80) - 720, (80 + 80 * targetCo.y) - 720, 1520, 1520);
					}
					targetShown=true;
				}
				if (target.width > 80) {
					target.x=target.x+80;
					target.y=target.y+80;
					target.width=target.width-160;
					target.height=target.height-160;
				}else{

					state=state+1;

				}
			}
			if(state==3){
				Future<Results> futRes = enemy.attack(targetCo.y,targetCo.x);
				Results res;
				try{
					res = futRes.get();
				}catch (Exception ex){res= new Results(false,new Ship(0,0,0,0),false);}

				if(res.hit){
					hits.add(new Rectangle((targetCo.x * 80) + 80 + 920, (targetCo.y * 80) + 80, 80, 80));
					Ship hitShip = res.sank;
					if(hitShip.size!=0){
						ships.add(new Rectangle(hitShip.x*80+1000, hitShip.y*80+80, 80+(abs(1-hitShip.up))*(hitShip.size-1)*80, 80+hitShip.up*(hitShip.size-1)*80));
					}
					if(res.lost){
						messageString="Click back to exit game.";
						state = 13;
						gameRunning=false;
					}else {
						messageString="Hit! You get another attack.";
						state = 4;
					}
				}else {
					messageString="Miss! It's the enemy's turn now.";
					misses.add(new Rectangle((targetCo.x * 80) + 80 + 920, (targetCo.y * 80) + 80, 80, 80));
					state=5;
				}
			}
			if(state==4||state==5||state==10||state==11) {
				if (wait < waitingTime / 2) {
					wait++;
				} else {
					if (target.width < 1520) {
						target.x = target.x - 80;
						target.y = target.y - 80;
						target.width = target.width + 160;
						target.height = target.height + 160;
					} else {
						targetShown=false;
						wait = 0;
						if(state==4){
							state=1;
						}
						if(state==10){
							state=7;
						}
						if(state==5||state==11){
							state=state+1;
						}
					}
				}
			}


		}else{	// campo di disposizione
			batch.setProjectionMatrix(camera.combined);
			batch.begin();
			batch.draw(gridImage, 0, 0);
			for (Rectangle sub:ships) {
				switch ((int)sub.width){
					case 80 : switch ((int) sub.height){
						case 80 : batch.draw(subImage, sub.x, sub.y, sub.width, sub.height); break;
						case 160 : batch.draw(torpImageRot, sub.x, sub.y, sub.width, sub.height); break;
						case 240 : batch.draw(incrImageRot, sub.x, sub.y, sub.width, sub.height); break;
						case 320 : batch.draw(portaImageRot, sub.x, sub.y, sub.width, sub.height); break;
					}  break;
					case 160 : 	batch.draw(torpImage, sub.x, sub.y, sub.width, sub.height); break;
					case 240 : 	batch.draw(incrImage, sub.x, sub.y, sub.width, sub.height); break;
					case 320 : 	batch.draw(portaImage, sub.x, sub.y, sub.width, sub.height); break;
				}
			}
			batch.end();
			stage.draw();

			if(Gdx.input.isTouched()) {
				touchPos.set(Gdx.input.getX(), Gdx.input.getY(), 0);
				camera.unproject(touchPos);

				lockedTime++;

				if(locked){ //if was already clicking on a ship (i.e I'm dragging a ship)
					//the new position should be inside the grid, and in one square (80px steps)
					float newX = touchPos.x - (touchPos.x%80);
					float newY = touchPos.y - (touchPos.y%80) ;
					if(newX<80) newX=80;
					if(newX>800) newX=800;
					if(newY<80) newY=80;
					if(newY>800) newY=800;
					ships.get(activeSub).x = newX;
					ships.get(activeSub).y = newY;
				}else { // if I was not clicking on a ship
					//check if I tap on a ship
					int index = -1;
					for (Rectangle sub : ships
							) {
						index++;
						if (sub.x < touchPos.x & touchPos.x < (sub.x + sub.width)
								& sub.y < touchPos.y & touchPos.y < (sub.y + sub.height)) {
							//if i click inside the margins of a ship, that ship is the active one
							locked = true;
							activeSub = index;
						}
					}

				}

			}else{
				if(locked & lockedTime<9){	// 9 frames in 60fps: 150 milliseconds (one tap)
					//rotate ship
					float aux = ships.get(activeSub).width;
					//noinspection SuspiciousNameCombination
					ships.get(activeSub).width=ships.get(activeSub).height;
					ships.get(activeSub).height=aux;
				}
				locked=false;
				lockedTime=0;
			}
		}

	}

	@Override
	public void dispose () {
		batch.dispose();
		fireImage.dispose();
		waterImage.dispose();
		subImage.dispose();
		torpImage.dispose();
		incrImage.dispose();
		portaImage.dispose();
	}

	private int controllaErrori(){
		int fuori = 0;
		int sovra = 0;
		int i = 0;
		while (fuori!=1 & i<10){
			float xborder = ships.get(i).x + ships.get(i).width;
			float yborder = ships.get(i).y + ships.get(i).height;
			if (xborder > 880 | yborder > 880) fuori = 1;
			i++;
		}
		i=0;
		while (sovra!=1 & i<10){
			float bottom = ships.get(i).y;
			float top = bottom + ships.get(i).height;
			float left = ships.get(i).x;
			float right = left + ships.get(i).width;
			int j=0;
			for (Rectangle sub : ships) {
				float bottomBis = sub.y;
				float topBis = bottomBis + sub.height;
				float leftBis = sub.x;
				float rightBis = leftBis + sub.width;

				if (i!=j & topBis>bottom & bottomBis<top & rightBis>left & leftBis<right){
					sovra=1;
				}
				j++;
			}
			i++;
		}
		return fuori + sovra*2;
	}

	private void computeMatrix(){
		Gdx.input.setInputProcessor(this);
		int i =0;
		for (Rectangle sub: ships
			 ) {
			i++;

			int x = (((int)sub.x) / 80) -1;
			int y = (((int)sub.y) / 80) -1;
			int width =((int)sub.width) / 80;
			int height = ((int)sub.height) / 80;
			for (int j = 0; j < width; j++) {
				disposizione[y][x+j]= i;
			}
			for (int j = 0; j < height; j++) {
				disposizione[y+j][x]= i;
			}

		}
		
	}

	private Coord convertiCoordinate(float x, float y){
		if (x>920) x=x-920;
		return new Coord(((int)x-80)/80, ((int)y-80)/80);
	}

	private boolean hit(int y, int x){
		if(disposizione[y][x]!=0 && disposizione[y][x]<100) {
			disposizione[y][x]+=100;
			return true;
		}
		return false;
	}

	private Ship sank(int a, int b){
		int y=a;
		int x=b;
		int num = disposizione[y][x];
		int up = 0;
		int size= 0;
		if (num<101) {
			return new Ship(0, 0, 0, 0);
		}else{
			for (int i = 0; i < 10; i++) { // i is the y coordinate
				for (int j = 0; j < 10; j++) { // j is the x coordinate
					if(disposizione[i][j]==num-100){
						return new Ship(0, 0, 0, 0);
					}
					if(disposizione[i][j]==num){
						size++; //valid only if ship sank
					}
				}
			}
		}
		if(y<9){
			if(num==disposizione[y+1][x]){
				up=1;
			}
		}
		if(y>0){
			while(y>0 && num==disposizione[y-1][x]){
				--y;
				up=1;
			}
		}
		if(x>0){
			while(x>0 && num==disposizione[y][x-1]){
				--x;
			}
		}
		return new Ship(y, x, size, up);
	}

	private boolean lost(){
		for (int i = 0; i < 10; i++) {
			for (int j = 0; j < 10; j++) {
				if(disposizione[i][j]>0 & disposizione[i][j]<100){
					return false;
				}
			}
		}
		return true;
	}

	@Override
	public boolean keyUp(int keycode) {
		return false;
	}

	@Override
	public boolean keyDown(int keycode) {
		return false;
	}

	@Override
	public boolean keyTyped(char character) {
		return false;
	}

	@Override
	public boolean touchDown(int screenX, int screenY, int pointer, int button) {
		return false;
	}

	@Override
	public boolean touchUp(int screenX, int screenY, int pointer, int button) {
		if(state==1) {
			touchPos.set(Gdx.input.getX(), Gdx.input.getY(), 0);
			camera.unproject(touchPos);

			if (1000 < touchPos.x & touchPos.x < 1800 & 80 < touchPos.y & touchPos.y < 880) {
				Coord co = convertiCoordinate(touchPos.x, touchPos.y);

				//NOTE! graphics want x,y coordinates while Ship and Adversary want y,x coordinates!
				if (!avversarioToccato[co.y][co.x]) {
					avversarioToccato[co.y][co.x] = true;
					state=2;
					targetCo = new Coord(co.x, co.y);

				}
			}
		}
		if(state==13 || state==14){
			Gdx.app.log("OVER","clicking");
			Gdx.app.exit();
		}
		return false;
	}

	@Override
	public boolean touchDragged(int screenX, int screenY, int pointer) {
		return false;
	}

	@Override
	public boolean mouseMoved(int screenX, int screenY) {
		return false;
	}

	@Override
	public boolean scrolled(int amount) {
		return false;
	}

	static boolean getRunning(){
		return (gameRunning);
	}
}
