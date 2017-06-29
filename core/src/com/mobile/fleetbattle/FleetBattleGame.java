package com.mobile.fleetbattle;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.InputProcessor;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.scenes.scene2d.ui.TextArea;
import com.badlogic.gdx.utils.Array;

import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.utils.viewport.StretchViewport;

import java.util.concurrent.Future;

import static java.lang.Math.abs;

public class FleetBattleGame extends ApplicationAdapter implements InputProcessor{
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

	private OrthographicCamera camera;
	private SpriteBatch batch;
	private Vector3 touchPos;
	private Skin skin;
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

	//state variables
	private boolean animateTarget=false;
	private boolean ready=true;
	private boolean endMyTurn=false;
	private boolean startMyTurn=true;
	private boolean enemyTurn=false;
	private boolean newElement=false;
	private boolean attackSet=false;
	private boolean enemyAttackSet=false;
	private	boolean secondHit=false;

	private int wait=0;
	private final int waitingTime=60; //60 frames = 1 sec


	private int[][] disposizione;
	private boolean disposto= false;
	private boolean[][] avversarioToccato;



	private class Coord{
		int x;
		int y;
		public Coord(int a, int b){
			x=a; y=b;
		}
	}

	public FleetBattleGame(com.mobile.fleetbattle.Adversary en){
		enemy = en;
	}

	@Override
	public void create () {
		skin = new Skin(Gdx.files.internal("uiskin.json"));

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

		camera = new OrthographicCamera();
		camera.setToOrtho(false, 920, 1120);
		touchPos = new Vector3();

		batch = new SpriteBatch();
		StretchViewport viewp = new StretchViewport(920, 1120, camera);
		stage = new Stage(viewp,batch);


		misses = new Array<Rectangle>();
		hits = new Array<Rectangle>();
		//ships default positions
		ships = new Array<Rectangle>();
		for (int i = 0; i < 4; i++) {
			Rectangle aux = new Rectangle();
			aux.x = 240;
			aux.y = 80 + 80*i;
			aux.width = 80;
			aux.height = 80;
			ships.add(aux);
		}
		for (int i = 0; i < 3; i++) {
			Rectangle aux = new Rectangle();
			aux.x = 320;
			aux.y = 80 + 80*i;
			aux.width = 160;
			aux.height = 80;
			ships.add(aux);
		}
		for (int i = 0; i < 2; i++) {
			Rectangle aux = new Rectangle();
			aux.x = 480;
			aux.y = 80 + 80*i;
			aux.width = 240;
			aux.height = 80;
			ships.add(aux);
		}
		for (int i = 0; i < 1; i++) {
			Rectangle aux = new Rectangle();
			aux.x = 80;
			aux.y = 560;
			aux.width = 320;
			aux.height = 80;
			ships.add(aux);
		}

		final TextButton button = new TextButton("Start Battle!", skin, "default");
		button.setWidth(400);
		button.setHeight(80);
		button.setPosition(460-200, 1000);
		button.getLabel().setFontScale(4, 4);

		button.addListener(new ClickListener(){
			@Override
			public void clicked(InputEvent event, float x, float y){
				int errori = controllaErrori();
				switch (errori){
					case 0 : computeMatrix(); disposto=true; break;
					case 1 : button.setText("Navi fuori dai bordi!"); break;
					case 2 : button.setText("Navi sovrapposte"); break;
					case 3 : button.setText("Navi sovrapposte e fuori dai bordi"); break;
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

		if(disposto){
			//enemy's turn
			if(enemyTurn && !enemyAttackSet && !newElement && !animateTarget){
				if(secondHit && wait<waitingTime/2){
					wait++;
				}else {
					Future<Ship> futAttack = enemy.getAttack();
					Ship attack;
					while (!futAttack.isDone()) {
						//ask to wait
					}
					try {
						attack = futAttack.get();
					} catch (Exception ex) {
						attack = new Ship(0, 0, 0, 0);
					}
					wait=0;
					enemyAttackSet = true;
					animateTarget = true;
					targetCo = new Coord(attack.x, attack.y);
				}

			}
			if(!animateTarget && enemyAttackSet) {
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
						misses.add(new Rectangle(160 , 980 + 80, 80, 80));
					}
				}else{
					secondHit=false;
					misses.add(new Rectangle((x * 80) + 80 , (y * 80) + 80, 80, 80));
					enemyTurn=false;
					startMyTurn=true;
				}
				newElement = true;
				animateTarget = true;
				enemyAttackSet = false;

			}
			// Transitions between turns
			if(startMyTurn && !newElement ) {
				if(wait<waitingTime){
					wait=wait+1;
				}else {
					if (camera.position.x < 920 + camera.viewportWidth / 2) {
						camera.translate(+40, 0, 0);
					} else {
						wait=0;
						startMyTurn = false;
						ready = true;
					}
					camera.update();
				}
			}
			if(endMyTurn && !newElement) {
				if(wait<waitingTime){
					wait=wait+1;
				}else {
					if (camera.position.x > camera.viewportWidth / 2) {
						camera.translate(-40, 0, 0);
					} else {
						wait=0;
						endMyTurn = false;
						enemyTurn = true;
					}
					camera.update();
				}
			}

			//Draw everything

			batch.setProjectionMatrix(camera.combined);
			batch.begin();
			batch.draw(gridImage, 0, 0);
			batch.draw(gridImage, 920, 0);
			if(targetShown){
				batch.draw(targetImage, target.x, target.y, target.width,target.height);
			}
			for (Rectangle miss:misses) {
				batch.draw(waterImage, miss.x, miss.y);
			}
			for (Rectangle hit:hits) {
				batch.draw(fireImage, hit.x, hit.y);
			}
			for (Rectangle sub:ships) {
				switch ((int)sub.width){
					case 80 : switch ((int) sub.height){
						case 80 : batch.draw(subImage, sub.x, sub.y); break;
						case 160 : batch.draw(torpImageRot, sub.x, sub.y); break;
						case 240 : batch.draw(incrImageRot, sub.x, sub.y); break;
						case 320 : batch.draw(portaImageRot, sub.x, sub.y); break;
					}  break;
					case 160 : 	batch.draw(torpImage, sub.x, sub.y); break;
					case 240 : 	batch.draw(incrImage, sub.x, sub.y); break;
					case 320 : 	batch.draw(portaImage, sub.x, sub.y); break;
				}
			}
			batch.end();

			if(animateTarget && (attackSet || enemyAttackSet) ){
				if(!targetShown){
					if(attackSet){
						target = new Rectangle((1000 + targetCo.x * 80) - 720, (80 + 80 * targetCo.y) - 720, 1520, 1520);
					}else {
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
					animateTarget=false;
				}
			}
			if(!animateTarget && attackSet){
				Future<Results> futRes = enemy.attack(targetCo.y,targetCo.x);
				Results res;
				while (!futRes.isDone()) {
					//ask to wait
				}
				try{
					res = futRes.get();
				}catch (Exception ex){res= new Results(false,new Ship(0,0,0,0),false);}

				if(res.hit){
					hits.add(new Rectangle((targetCo.x * 80) + 80 + 920, (targetCo.y * 80) + 80, 80, 80));
					Ship hitShip = res.sank;
					if(hitShip.size!=0){
						ships.add(new Rectangle(hitShip.x*80+1000, hitShip.y*80+80, 80+(abs(1-hitShip.up))*(hitShip.size-1)*80, 80+hitShip.up*(hitShip.size-1)*80));
						if(res.lost){
							hits.add(new Rectangle(920,920,80,80));
						}
					}
					ready=true;
				}else {
					misses.add(new Rectangle((targetCo.x * 80) + 80 + 920, (targetCo.y * 80) + 80, 80, 80));
					endMyTurn=true;
				}
				newElement=true;
				animateTarget=true;
				attackSet=false;
			}
			if(animateTarget && newElement) {
				if (wait < waitingTime / 2) {
					wait++;
				} else {
					if (target.width < 1520) {
						target.x = target.x - 80;
						target.y = target.y - 80;
						target.width = target.width + 160;
						target.height = target.height + 160;
					} else {
						wait = 0;
						targetShown = false;
						animateTarget = false;
						newElement = false;
					}
				}
			}

			//Player turn - must touch a square
//			if(!animateTarget && ready && Gdx.input.justTouched()) {
//				touchPos.set(Gdx.input.getX(), Gdx.input.getY(), 0);
//				camera.unproject(touchPos);
//
//				if(1000<touchPos.x& touchPos.x<1800 & 80<touchPos.y & touchPos.y<880) {
//					Coord co = convertiCoordinate(touchPos.x, touchPos.y);
//
//					//NOTE! graphics want x,y coordinates while Ship and Adversary want y,x coordinates!
//					if (!avversarioToccato[co.y][co.x]) {
//						avversarioToccato[co.y][co.x]=true;
//						ready=false;
//						attackSet=true;
//						animateTarget=true;
//						targetCo=new Coord(co.x,co.y);
//
//					}
//				}
//			}


		}else{	// campo di disposizione
			batch.setProjectionMatrix(camera.combined);
			batch.begin();
			batch.draw(gridImage, 0, 0);
			for (Rectangle sub:ships) {
				switch ((int)sub.width){
					case 80 : switch ((int) sub.height){
						case 80 : batch.draw(subImage, sub.x, sub.y); break;
						case 160 : batch.draw(torpImageRot, sub.x, sub.y); break;
						case 240 : batch.draw(incrImageRot, sub.x, sub.y); break;
						case 320 : batch.draw(portaImageRot, sub.x, sub.y); break;
					}  break;
					case 160 : 	batch.draw(torpImage, sub.x, sub.y); break;
					case 240 : 	batch.draw(incrImage, sub.x, sub.y); break;
					case 320 : 	batch.draw(portaImage, sub.x, sub.y); break;
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

	public Ship sank(int a, int b){
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
		if(!animateTarget && ready) {
			touchPos.set(Gdx.input.getX(), Gdx.input.getY(), 0);
			camera.unproject(touchPos);

			if (1000 < touchPos.x & touchPos.x < 1800 & 80 < touchPos.y & touchPos.y < 880) {
				Coord co = convertiCoordinate(touchPos.x, touchPos.y);

				//NOTE! graphics want x,y coordinates while Ship and Adversary want y,x coordinates!
				if (!avversarioToccato[co.y][co.x]) {
					avversarioToccato[co.y][co.x] = true;
					ready = false;
					attackSet = true;
					animateTarget = true;
					targetCo = new Coord(co.x, co.y);

				}
			}
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
}
