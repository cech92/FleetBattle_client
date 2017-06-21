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

	private OrthographicCamera camera;
	private SpriteBatch batch;
	private Vector3 touchPos;

	private Array<Rectangle> ships;


	private boolean locked = false;
	private int activeSub = 0;

	@Override
	public void create () {
		fireImage = new Texture(Gdx.files.internal("fire.png"));
		waterImage = new Texture(Gdx.files.internal("water.png"));
		portaImage = new Texture(Gdx.files.internal("porta.png"));
		incrImage = new Texture(Gdx.files.internal("incr.png"));
		torpImage = new Texture(Gdx.files.internal("torp.png"));
		subImage = new Texture(Gdx.files.internal("sub.png"));

		camera = new OrthographicCamera();
		camera.setToOrtho(false, 800, 800);
		touchPos = new Vector3();

		batch = new SpriteBatch();

		//ships default position
		ships = new Array<Rectangle>();
		for (int i = 0; i < 4; i++) {
			Rectangle aux = new Rectangle();
			aux.x = 200;
			aux.y = 80*i;
			aux.width = 80;
			aux.height = 80;
			ships.add(aux);
		}
		for (int i = 0; i < 3; i++) {
			Rectangle aux = new Rectangle();
			aux.x = 320;
			aux.y = 80*i;
			aux.width = 160;
			aux.height = 80;
			ships.add(aux);
		}
		for (int i = 0; i < 2; i++) {
			Rectangle aux = new Rectangle();
			aux.x = 480;
			aux.y = 80*i;
			aux.width = 240;
			aux.height = 80;
			ships.add(aux);
		}
		for (int i = 0; i < 1; i++) {
			Rectangle aux = new Rectangle();
			aux.x = 80;
			aux.y = 480;
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
		for (Rectangle sub:ships) {
			switch ((int)sub.width){
				case 80 : batch.draw(subImage, sub.x, sub.y); break;
				case 160 : 	batch.draw(torpImage, sub.x, sub.y); break;
				case 240 : 	batch.draw(incrImage, sub.x, sub.y); break;
				case 320 : 	batch.draw(portaImage, sub.x, sub.y); break;
			}
		}
		batch.end();

		if(Gdx.input.isTouched()) {
			touchPos.set(Gdx.input.getX(), Gdx.input.getY(), 0);
			camera.unproject(touchPos);


			if(locked){
				ships.get(activeSub).x = touchPos.x - (touchPos.x%80);
				ships.get(activeSub).y = touchPos.y - (touchPos.y%80);
			}else {
				int index = -1;
				for (Rectangle sub : ships
						) {
					index++;
					if (sub.x < touchPos.x & touchPos.x < (sub.x + sub.width)
							& sub.y < touchPos.y & touchPos.y < (sub.y + sub.height)) {
						locked = true;
						activeSub = index;
					}
				}

			}

		}else{
			locked=false;
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
