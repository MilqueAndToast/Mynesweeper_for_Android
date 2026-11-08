package com.tjstudios.mynesweeper.desktop;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.backends.lwjgl3.Lwjgl3Application;
import com.badlogic.gdx.backends.lwjgl3.Lwjgl3ApplicationConfiguration;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.PixmapIO;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Timer;
import com.tjstudios.mynesweeper.Mynesweeper;
import java.lang.reflect.Field;

/** Runs the actual graphics backend and exercises taps and repeated restarts. */
public class SmokeLauncher extends Mynesweeper {
    private int frame;
    private int idleFrames;
    private int firstTickFrame;
    private boolean scriptStarted;

    @Override public void create() {
        super.create();
        if (Gdx.graphics.isContinuousRendering()) throw new AssertionError("Rendering is continuous");
        Timer.schedule(new Timer.Task() {
            @Override public void run() {
                if (idleFrames == 0 || idleFrames > 4) throw new AssertionError("Expected an idle board after startup");
                scriptStarted = true;
                Gdx.graphics.requestRendering();
            }
        }, .35f);
        Timer.schedule(new Timer.Task() {
            @Override public void run() { throw new AssertionError("Rendering smoke test did not finish"); }
        }, 8f);
    }
    private Object read(String name) {
        try { Field f=Mynesweeper.class.getDeclaredField(name); f.setAccessible(true); return f.get(this); }
        catch(Exception e) { throw new RuntimeException(e); }
    }
    private void tap(Actor a) {
        Stage s=(Stage)read("stage");
        if(frame==1) {
            float header=(Float)read("topRectHeight");
            if(header > Gdx.graphics.getHeight()*.07001f) throw new AssertionError("Header exceeds 7%");
            Actor tile=s.getActors().get(2);
            if(Math.abs(tile.getWidth()-tile.getHeight())>.01f) throw new AssertionError("Tiles are not square");
            if((Float)read("boardBottom") < (Float)read("btmRectHeight")-.01f)
                throw new AssertionError("Board overlaps controls");
            float lowerGap=(Float)read("boardBottom")-(Float)read("btmRectHeight");
            float upperGap=Gdx.graphics.getHeight()-header-(Float)read("boardBottom")
                    -(Float)read("sqSide")*11;
            if(Math.abs(upperGap-lowerGap)>.01f) throw new AssertionError("Board is not vertically centered");
        }
        Vector2 p=a.localToStageCoordinates(new Vector2(a.getWidth()/2,a.getHeight()/2));
        s.stageToScreenCoordinates(p); s.touchDown((int)p.x,(int)p.y,0,0); s.touchUp((int)p.x,(int)p.y,0,0);
    }
    @Override public void render() {
        super.render();
        if (!scriptStarted) {
            idleFrames++;
            return;
        }
        frame++;
        Stage s=(Stage)read("stage");
        if(frame==1) tap(s.getActors().get(2));
        if(frame==4 && (Boolean)read("lossCheck")) throw new AssertionError("Unsafe first tap");
        if(frame==5) tap(s.getActors().get(0));
        if(frame==7 && (Integer)read("toggleButtonIndex")!=1) throw new AssertionError("Toggle failed");
        if(frame==8 || frame==12 || frame==16) tap(s.getActors().get(1));
        if(frame==20) {
            if((Integer)read("numRevealed")!=0 || (Boolean)read("winCheck") || (Boolean)read("lossCheck"))
                throw new AssertionError("Restart failed");
            if(Gdx.input.getInputProcessor()!=read("stage")) throw new AssertionError("Restart lost input");
            tap(s.getActors().get(2));
        }
        if (frame < 20) Gdx.graphics.requestRendering();
        // The first reveal and clock must wake the renderer without help from the test.
        int seconds = (Integer)read("secTimer");
        if (frame > 20 && seconds == 1 && firstTickFrame == 0) firstTickFrame = frame;
        if (frame > 20 && seconds >= 2) {
            if (firstTickFrame == 0 || frame - firstTickFrame > 3) {
                throw new AssertionError("Expected two timer ticks with no continuous rendering between them");
            }
            Pixmap pixels=Pixmap.createFromFrameBuffer(0,0,Gdx.graphics.getBackBufferWidth(),Gdx.graphics.getBackBufferHeight());
            PixmapIO.writePNG(Gdx.files.absolute(System.getProperty("smoke.image")),pixels,-1,true); pixels.dispose();
            System.out.println("PASS: first tap, mode toggle, three restarts, idle rendering, and two timer-driven redraws");
            Gdx.app.exit();
        }
    }
    public static void main(String[] args) {
        Lwjgl3ApplicationConfiguration c=new Lwjgl3ApplicationConfiguration();
        c.setTitle("Mynesweeper smoke test");
        c.setWindowedMode(Integer.getInteger("smoke.width",400),Integer.getInteger("smoke.height",720));
        c.disableAudio(true);
        new Lwjgl3Application(new SmokeLauncher(),c);
    }
}
