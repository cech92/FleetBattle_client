package com.mobile.fleetbattle;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
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
	private Skin skin;
	private Stage stage;

	private Array<Rectangle> ships;


	private boolean locked = false;
	private int lockedTime = 0;
	private int activeSub = 0;

	private int[][] disposizione;
	private boolean disposto= false;

	@Override
	public void create () {
		skin = new Skin(Gdx.files.internal("uiskin.json"));

		disposizione = new int[10][10];

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
					case 0 : scriviMatrice(); disposto=true; break;
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
			stage.draw();
		}else{
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

	private void scriviMatrice(){
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
		String disp="";
		for (int j = 9; j > -1; j--) {
			for (int k = 0; k < 10; k++) {
				disp += disposizione[j][k] + " ";
			}
			disp += "\n";
		}
		final TextArea matrice = new TextArea(disp,skin);
		matrice.setWidth(400);
		matrice.setHeight(400);
		matrice.setPosition(80, 400);
		stage.clear();
		stage.addActor(matrice);
		Gdx.input.setInputProcessor(stage);
	}
}
