package com.mobile.fleetbattle;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.Array;

public class FleetBattleGame extends ApplicationAdapter {
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

	private Array<Rectangle> ships;


	private boolean locked = false;
	private int lockedTime = 0;
	private int activeSub = 0;

	@Override
	public void create () {
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


	}

	@Override
	public void render () {
		Gdx.gl.glClearColor(1, 1, 0.8f, 1);
		Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

		camera.update();

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
				ships.get(activeSub).width=ships.get(activeSub).height;
				ships.get(activeSub).height=aux;
			}
			locked=false;
			lockedTime=0;
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
}
