package com.tjstudios.mynesweeper;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.freetype.FreeTypeFontGenerator;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.InputListener;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.utils.ActorGestureListener;
import com.badlogic.gdx.utils.viewport.Viewport;
import com.badlogic.gdx.utils.viewport.FitViewport;
import java.util.ArrayList;

public class Mynesweeper extends Game {
    private int endDelay;
    private int gridWidth, gridHeight;
    private int WIN_WIDTH, WIN_HEIGHT;
    private int bombCount, curFlagCount, secTimer, mineCounter, numRevealed;
    private int toggleButtonIndex;
    private int clusterNum;
    private int[][] mineVals;
    private float MINE_X_SIZE, MINE_Y_SIZE;
    private float sqSide;
    private float timer;
    private float btmRectHeight, topRectHeight;
    private float boardLeft, boardBottom;
    private boolean timerCheck;
    private boolean firstReveal;
    private boolean lossCheck, winCheck;
    private String[] mineStatus, toggleButtonText;
    private ShapeRenderer shape;
    private OrthographicCamera camera;
    private Viewport viewport;
	private SpriteBatch batch;                                                                              // Used for spriteLogo initialization
	private Texture img;
    private BitmapFont clockFont, ubuntuFont;
    private Stage stage;
    private Skin toggleSkin, mineSkin;
    private GlyphLayout timerLayout, bombLayout;
    private MyButton toggleButton;
    private Color toggleButtonColor, toggleButtonShaded, toggleButtonClicked;
    private Color mineColor, mineColorShaded;
    private ArrayList<TextButton> mineField = new ArrayList<TextButton>();
    private ArrayList<MineButton> mineFieldValues = new ArrayList<MineButton>();

    @Override
	public void create() {
        storeWindowAndPosition();   // Stores window size and position into their own variables
        initFont();                 // Creates freetype font and sets its properties
        initVars();
        initButtonValues();
        initButtons();
        initGrid();
        setBackground();    // Sets background color

        Gdx.input.setInputProcessor(stage);
//        stage.setDebugAll(true);
	}

	@Override
	public void render() {
        super.render();

        setBackground();
        cameraSetup();
        incrementTimer();
        gridProcess();
        bombText();
        shapeProcess();
        batchProcess();
        stageProcess();
	}
	
	@Override
	public void dispose() {
		batch.dispose();
		img.dispose();
        toggleSkin.dispose();
        stage.dispose();
        mineSkin.dispose();
        clockFont.dispose();
        shape.dispose();
	}

	private void batchProcess() {
        batch.begin();
            clockFont.draw(batch, bombLayout, WIN_WIDTH/4 - bombLayout.width/2, WIN_HEIGHT-topRectHeight/2+bombLayout.height/2);
            clockFont.draw(batch, timerLayout, WIN_WIDTH*3/4 - timerLayout.width/2, WIN_HEIGHT-topRectHeight/2+timerLayout.height/2);
        batch.end();
    }

    private void shapeProcess() {
        shape.setColor(Color.LIGHT_GRAY);
        shape.begin(ShapeRenderer.ShapeType.Filled);
        shape.rect(boardLeft, boardBottom, gridWidth * sqSide, gridHeight * sqSide);
        shape.end();
    }

    private void stageProcess(){
        stage.act();
        stage.draw();
    }

    private void roundedRect(Pixmap toggleButtonPixmap, Color c, int x, int y, int width, int height, int radius){
        // Central rectangle
        toggleButtonPixmap.setColor(c);
        toggleButtonPixmap.fillRectangle(x + radius, y + radius, width - 2*radius, height - 2*radius);

        // Four side rectangles, in clockwise order
        toggleButtonPixmap.fillRectangle(x + radius, y, width - 2*radius, radius);
        toggleButtonPixmap.fillRectangle(x + width - radius, y + radius, radius, height - 2*radius);
        toggleButtonPixmap.fillRectangle(x + radius, y + height - radius, width - 2*radius, radius);
        toggleButtonPixmap.fillRectangle(x, y + radius, radius, height - 2*radius);

        // Four arches, clockwise too
        toggleButtonPixmap.fillCircle(x + radius, y + radius, radius);
        toggleButtonPixmap.fillCircle(x + width - radius, y + radius, radius);
        toggleButtonPixmap.fillCircle(x + width - radius, y + height - radius, radius);
        toggleButtonPixmap.fillCircle(x + radius, y + height - radius, radius);
    }

    @Override
    public void resize(int width, int height) {
        viewport.update(width, height, true);
    }

    @Override
    public void pause() {
    }

    @Override
    public void resume() {
    }

    private void setBackground(){
        Gdx.gl.glClearColor(0, 0, 0, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT | GL20.GL_DEPTH_BUFFER_BIT);
    }

    private void initVars(){
        lossCheck = false;
        winCheck = false;
        firstReveal = true;
        endDelay = 0;
        camera = new OrthographicCamera(WIN_WIDTH, WIN_HEIGHT);
        viewport = new FitViewport(WIN_WIDTH, WIN_HEIGHT, camera);
        viewport.update(Gdx.graphics.getWidth(), Gdx.graphics.getHeight(), true);
        shape = new ShapeRenderer();
        stage = new Stage(viewport);
        batch = new SpriteBatch();
        img = new Texture("minesweep.png");
        mineStatus = new String[]{" ", "1", "2", "3", "4", "5", "6", "7", "8", "BOMB"};             // 0 is blank, 9 is bomb and 1-8 are adjacent bomb counts
        toggleButtonText = new String[]{"REVEAL", "FLAG"};
        timerLayout = new GlyphLayout();
        bombLayout = new GlyphLayout();
        mineField.clear();
        mineFieldValues.clear();
        toggleButtonIndex = 0;
        gridWidth = 8;
        gridHeight = 11;
        topRectHeight = WIN_HEIGHT * .07f;
        btmRectHeight = WIN_HEIGHT / 8f;
        sqSide = Math.min(WIN_WIDTH / (float)gridWidth,
                (WIN_HEIGHT - topRectHeight - btmRectHeight) / gridHeight);
        boardLeft = (WIN_WIDTH - sqSide * gridWidth) / 2f;
        boardBottom = WIN_HEIGHT - topRectHeight - sqSide * gridHeight;
        bombCount = 20;
        curFlagCount = 20;
        mineCounter = 20;
        numRevealed = 0;
        secTimer = 0;
        timer = 0f;
        timerCheck = false;
        clusterNum = 0;
        MINE_X_SIZE = MINE_Y_SIZE = sqSide*.88f;
        toggleButtonColor = new Color(200f/255f, 200f/255f, 0, 1);
        toggleButtonShaded = new Color(179f/255f, 179f/255f, 0, 1);
        toggleButtonClicked = new Color(158f/255f, 158f/255f, 0, 1);
        mineColor = new Color(169f/255f, 169f/255f, 169f/255f, 1);
        mineColorShaded = new Color(150f/255f, 150f/255f, 150f/255f, 1);
    }

    private void initButtonValues() {
        toggleButton = new MyButtonRounded(WIN_WIDTH*(.05f), btmRectHeight/10, WIN_WIDTH*(.9f), btmRectHeight*8/10, 10f);
        float sqPos = (sqSide - MINE_X_SIZE) / 2f;

        for(int i = 0; i < gridHeight; i++) {
            for(int j = 0; j < gridWidth; j++) {
                mineFieldValues.add(new MineButton(boardLeft + j*sqSide + sqPos,
                        boardBottom + i*sqSide + sqPos, MINE_X_SIZE, MINE_Y_SIZE, mineStatus[9], j, i, false));
            }
        }
    }

    private void initButtons() {
        toggleSkin = new Skin();
        Pixmap toggleButtonPixmap = new Pixmap((int)toggleButton.getXSize(), (int)toggleButton.getYSize(), Pixmap.Format.RGBA8888);
        roundedRect(toggleButtonPixmap, toggleButtonColor, 0, 0, (int)toggleButton.getXSize(), (int)toggleButton.getYSize(), (int)(((MyButtonRounded)toggleButton).getRadius()*Gdx.graphics.getDensity()));
        toggleSkin.add("yellow", new Texture(toggleButtonPixmap));
        toggleButtonPixmap.dispose();
        toggleSkin.add("default", ubuntuFont);

        TextButton.TextButtonStyle toggleStyle = new TextButton.TextButtonStyle();
        toggleStyle.up = toggleSkin.newDrawable("yellow", toggleButtonColor);
        toggleStyle.down = toggleSkin.newDrawable("yellow", toggleButtonClicked);
        toggleStyle.over = toggleSkin.newDrawable("yellow", toggleButtonShaded);
        toggleStyle.font = toggleSkin.getFont("default");
        toggleSkin.add("default", toggleStyle);

        final TextButton tB = new TextButton(toggleButtonText[toggleButtonIndex], toggleSkin);
        tB.setPosition(toggleButton.getX(), toggleButton.getY());
        tB.setSize(WIN_WIDTH*.58f, toggleButton.getYSize());
        tB.addListener(new InputListener() {
            @Override
            public boolean touchDown (InputEvent event, float x, float y, int pointer, int button) {
                toggleButtonIndex = Math.abs(toggleButtonIndex - 1);
                tB.setText(toggleButtonText[toggleButtonIndex]);

                return true;
            }

            public void touchUp (InputEvent event, float x, float y, int pointer, int button) {  }
        });

        tB.addListener(new ActorGestureListener() {
            @Override
            public boolean longPress(Actor actor, float x, float y) {
                Gdx.app.postRunnable(new Runnable() {
                    public void run() { restart(); }
                });

                return true;
            }
        });

        stage.addActor(tB);
        TextButton newGame = new TextButton("NEW", toggleSkin);
        newGame.setBounds(WIN_WIDTH*.66f, toggleButton.getY(), WIN_WIDTH*.29f, toggleButton.getYSize());
        newGame.addListener(new com.badlogic.gdx.scenes.scene2d.utils.ClickListener() {
            @Override public void clicked(InputEvent event, float x, float y) {
                Gdx.app.postRunnable(new Runnable() {
                    public void run() { restart(); }
                });
            }
        });
        stage.addActor(newGame);

        /*------------------------------------------------------------------------------------------------------------------------------------*/

        mineSkin = new Skin();
        Pixmap minePix = new Pixmap((int)MINE_X_SIZE, (int)MINE_Y_SIZE, Pixmap.Format.RGBA8888);
        TextButton.TextButtonStyle mineStyle;

        minePix.setColor(Color.WHITE);
        minePix.fill();
        mineSkin.add("gray", new Texture(minePix));
        mineSkin.add("white", new Texture(minePix));
        mineSkin.add("red", new Texture(minePix));
        for(int i = 0; i < mineFieldValues.size(); i++) {
            mineStyle = new TextButton.TextButtonStyle();
            mineStyle.up = mineSkin.newDrawable("gray", mineColor);
            mineStyle.down = mineSkin.newDrawable("gray", mineColorShaded);
            mineStyle.font = ubuntuFont;

            mineSkin.add("default", mineStyle);

            final TextButton temp = new TextButton("", mineSkin);
            temp.setName("hidden");
            temp.setPosition(mineFieldValues.get(i).getX(), mineFieldValues.get(i).getY());
            temp.setSize(MINE_X_SIZE, MINE_Y_SIZE);

            temp.addListener(new InputListener() {
                @Override
                public boolean touchDown(InputEvent event, float x, float y, int pointer, int button) {
                    if(lossCheck || winCheck) { return true; }
                    if(!timerCheck) { timerCheck = true; }

                    if(temp.getName().equals("hidden") || temp.getName().equals("flagged") || temp.getName().equals("flag")) {
                        if(toggleButtonIndex == 0 && temp.getName().equals("hidden")) {   // if ToggleButton's text is "BOMB"
                            if(firstReveal) {
                                makeFirstRevealSafe(mineField.indexOf(temp));
                                firstReveal = false;
                            }
                            temp.setName("revealed");
                            numRevealed++;
                            temp.getStyle().up = mineSkin.newDrawable("white", Color.WHITE);
                            temp.getStyle().down = mineSkin.newDrawable("white", Color.WHITE);
                        }
                        else if(toggleButtonIndex != 0) {  // if ToggleButton's text is "FLAG"
                            if(temp.getName().equals("flagged") || temp.getName().equals("flag")) {
                                temp.setName("unflagged");
                                temp.setText("");
                                curFlagCount++;
                                temp.getStyle().up = mineSkin.newDrawable("gray", mineColor);
                                temp.getStyle().down = mineSkin.newDrawable("gray", mineColorShaded);
                            }
                            else {
                                temp.setName("flagged");
                                temp.setText("F");
                                curFlagCount--;
                                temp.getStyle().up = mineSkin.newDrawable("red", Color.FIREBRICK);
                                temp.getStyle().down = mineSkin.newDrawable("red", Color.FIREBRICK);
                            }
                        }
                    }

                    return true;
                }
                @Override
                public void touchUp(InputEvent event, float x, float y, int pointer, int button) {  }
            });

            mineField.add(temp);
            stage.addActor(temp);
        }

        minePix.dispose();
    }

    private void initFont(){
        FreeTypeFontGenerator fontGen = new FreeTypeFontGenerator(Gdx.files.internal("fonts/digital_7.ttf"));
        FreeTypeFontGenerator.FreeTypeFontParameter param = new FreeTypeFontGenerator.FreeTypeFontParameter();
        // Keep the digits comfortably inside the fixed seven-percent header.
        param.size = Math.max(1, (int)Math.min(WIN_WIDTH/13f, WIN_HEIGHT*.07f*.45f));
        param.color = Color.RED;
        param.borderWidth = Math.max(1, param.size / 14);
        param.borderColor = Color.BLACK;
        param.minFilter = Texture.TextureFilter.Linear;
        param.magFilter = Texture.TextureFilter.Linear;
        clockFont = fontGen.generateFont(param);

        fontGen.dispose();
        fontGen = new FreeTypeFontGenerator(Gdx.files.internal("fonts/ubuntu_bold.ttf"));
        param = new FreeTypeFontGenerator.FreeTypeFontParameter();
        param.size = Math.max(12, WIN_WIDTH/16);
        param.color = Color.BLACK;
        param.minFilter = Texture.TextureFilter.Linear;
        param.magFilter = Texture.TextureFilter.Linear;
        ubuntuFont = fontGen.generateFont(param);
        fontGen.dispose();
    }

    private void initGrid() {
        mineVals = new int[gridWidth][gridHeight];
        double randX = Math.random();
        double randY = Math.random();

        for(int i = 0; i < bombCount ; i++) {
            if(mineVals[(int)(randX * gridWidth)][(int)(randY * gridHeight)] == 9) {
                i--;
            }
            else {
                mineVals[(int) (randX * gridWidth)][(int) (randY * gridHeight)] = 9;
            }
            randX = Math.random();
            randY = Math.random();
        }

        for(int i = 0; i < gridHeight; i++) {
            for(int j = 0; j < gridWidth; j++) {
                if(mineVals[j][i] != 9) {
                    mineVals[j][i] = bombCheck(j, i);
                }
            }
        }
    }

    private int bombCheck(int x, int y) {
        int value = 0;
        int minX = -1, maxX = 1;
        int minY = -1, maxY = 1;

        if(x == 0) { minX = 0; }
        if(x == gridWidth-1) { maxX = 0; }
        if(y == 0) { minY = 0; }
        if(y == gridHeight-1) { maxY = 0; }

        for(int i = minY; i <= maxY; i++) {
            for(int j = minX; j <= maxX; j++) {
                if(mineVals[x+j][y+i] == 9) {
                    value++;
                }
            }
        }

        return value;
    }

    private void makeFirstRevealSafe(int index) {
        int x = index % gridWidth, y = index / gridWidth;
        if(mineVals[x][y] != 9) { return; }
        outer: for(int row = 0; row < gridHeight; row++) {
            for(int col = 0; col < gridWidth; col++) {
                if(mineVals[col][row] != 9) {
                    mineVals[col][row] = 9;
                    mineVals[x][y] = 0;
                    break outer;
                }
            }
        }
        for(int row = 0; row < gridHeight; row++) {
            for(int col = 0; col < gridWidth; col++) {
                if(mineVals[col][row] != 9) { mineVals[col][row] = bombCheck(col, row); }
            }
        }
    }

    private void emptySpaceCheck(int x, int y) {
        ArrayList<Integer> pending = new ArrayList<Integer>();
        pending.add(y * gridWidth + x);
        for(int next = 0; next < pending.size(); next++) {
            int index = pending.get(next), cx = index % gridWidth, cy = index / gridWidth;
            for(int row = Math.max(0, cy-1); row <= Math.min(gridHeight-1, cy+1); row++) {
                for(int col = Math.max(0, cx-1); col <= Math.min(gridWidth-1, cx+1); col++) {
                    TextButton tile = mineField.get(row * gridWidth + col);
                    if(!tile.getName().equals("hidden") || mineVals[col][row] == 9) { continue; }
                    int value = mineVals[col][row];
                    tile.setName(value == 0 ? "empty" : "value");
                    tile.setText(value == 0 ? " " : String.valueOf(value));
                    tile.getStyle().up = mineSkin.newDrawable("white", Color.WHITE);
                    tile.getStyle().down = mineSkin.newDrawable("white", Color.WHITE);
                    numRevealed++;
                    if(value == 0) { pending.add(row * gridWidth + col); }
                }
            }
        }
    }

    private void gridProcess() {
        String revealString, revealName, flaggedName = "", unflaggedName = "";
        TextButton temp;

        for(int i = 0; i < gridHeight; i++) {
            for(int j = 0; j < gridWidth; j++) {
                temp = mineField.get(i * gridWidth + j);

                if(temp.getName().equals("revealed")) {
                    if(mineVals[j][i] == 0) {
                        revealString = " ";
                        revealName = "empty";
                        emptySpaceCheck(j, i);
                    }
                    else if ((mineVals[j][i] == 9)){
                        revealString = "B";
                        revealName = "bomb";
                        lossCheck = true;
                    }
                    else {
                        revealString = mineVals[j][i] + "";
                        revealName = "value";
                    }

                    temp.setText(revealString);
                    temp.setName(revealName);
                }

                else if(temp.getName().equals("flagged")) {
                    if ((mineVals[j][i] == 9)){
                        mineCounter--;
                    }

                    temp.setName("flag");
                }

                else if(temp.getName().equals("unflagged")) {
                    if ((mineVals[j][i] == 9)){
                        mineCounter++;
                    }

                    temp.setName("hidden");
                }

                if(!lossCheck && numRevealed == (gridWidth*gridHeight-bombCount)) {
                    winCheck = true;
                }
            }
        }
    }

    private void bombText() {
        if(lossCheck) {
            bombLayout.setText(clockFont, "YOU");
            timerLayout.setText(clockFont, "LOSE");
        }
        else if(winCheck) {
            bombLayout.setText(clockFont, "YOU");
            timerLayout.setText(clockFont, "WIN");
        }
        else {
            bombLayout.setText(clockFont, "Bombs:" + curFlagCount);
            timerLayout.setText(clockFont, "Time:" + secTimer);
        }
    }

    private void cameraSetup() {
        viewport.apply();
        camera.update();
        batch.setProjectionMatrix(camera.combined);
        shape.setProjectionMatrix(camera.combined);
    }

    private void restart() {
        dispose();
        initFont();
        initVars();
        initButtonValues();
        initButtons();
        initGrid();
        Gdx.input.setInputProcessor(stage);
    }

    private void storeWindowAndPosition(){
        WIN_HEIGHT = Gdx.graphics.getHeight();
        WIN_WIDTH = Gdx.graphics.getWidth();
    }

    private void incrementTimer(){
        if(timerCheck && !lossCheck && !winCheck && timer < 1000) {
            float deltaTime = Gdx.graphics.getDeltaTime();
            timer += deltaTime;
            secTimer = (int)timer;
        }
    }

    private static class MyButton {
        private float xPos, yPos, xSize, ySize;
        private String text;

        private MyButton(float x, float y, float xSize, float ySize) {
            xPos = x;
            yPos = y;
            this.xSize = xSize;
            this.ySize = ySize;
        }

        private MyButton(float x, float y, float xSize, float ySize, String theText) {
            xPos = x;
            yPos = y;
            this.xSize = xSize;
            this.ySize = ySize;
            text = theText;
        }

        float getX() {
            return xPos;
        }

        private void setX(float x) {
            xPos = x;
        }

        float getY() {
            return yPos;
        }

        private void setY(float y) {
            yPos = y;
        }

        float getXSize() {
            return xSize;
        }

        private void setXSize(float x) {
            xSize = x;
        }

        float getYSize() {
            return ySize;
        }

        private void setYSize(float y) {
            ySize = y;
        }

        String getText() {
            return text;
        }

        private void setText(String s) {
            text = s;
        }
    }

    private class MyButtonRounded extends MyButton {
        private float radius;

        private MyButtonRounded(float x, float y, float xSize, float ySize, float r) {
            super(x, y, xSize, ySize);
            radius = r;
        }

        private MyButtonRounded(float x, float y, float xSize, float ySize, String theText, float r) {
            super(x, y, xSize, ySize, theText);
            radius = r;
        }

        float getRadius() {
            return radius;
        }

        private void setRadius(float r) {
            radius = r;
        }
    }

    private class MineButton extends MyButton {
        private int xMinePos, yMinePos;
        private boolean revealed;

        private MineButton(float x, float y, float xSize, float ySize, String text, int xMinePos, int yMinePos, boolean revealed) {
            super(x, y, xSize, ySize, text);
            this.revealed = revealed;
            this.xMinePos = xMinePos;
            this.yMinePos = yMinePos;
        }

        public int getXMinePos() {
            return xMinePos;
        }

        private void setXMinePos(int x) {
            xMinePos = x;
        }

        public int getYMinePos() {
            return yMinePos;
        }

        private void setYMinePos(int y) {
            yMinePos = y;
        }

        public boolean getRevealed() {return revealed;}

        private void setRevealed(boolean revealed) {this.revealed = revealed;}
    }
}
