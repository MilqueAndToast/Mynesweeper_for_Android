package com.tjstudios.mynesweeper.desktop;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.backends.lwjgl3.Lwjgl3Application;
import com.badlogic.gdx.backends.lwjgl3.Lwjgl3ApplicationConfiguration;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.PixmapIO;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.math.Vector2;
import com.tjstudios.mynesweeper.Mynesweeper;
import java.lang.reflect.Field;

/** Runs the actual graphics backend and exercises taps and repeated restarts. */
public class SmokeLauncher extends Mynesweeper {
    private int frame;
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
        }
        Vector2 p=a.localToStageCoordinates(new Vector2(a.getWidth()/2,a.getHeight()/2));
        s.stageToScreenCoordinates(p); s.touchDown((int)p.x,(int)p.y,0,0); s.touchUp((int)p.x,(int)p.y,0,0);
    }
    @Override public void render() {
        super.render(); frame++;
        Stage s=(Stage)read("stage");
        if(frame==2) tap(s.getActors().get(2));
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
        if(frame==25) {
            Pixmap pixels=Pixmap.createFromFrameBuffer(0,0,Gdx.graphics.getBackBufferWidth(),Gdx.graphics.getBackBufferHeight());
            PixmapIO.writePNG(Gdx.files.absolute(System.getProperty("smoke.image")),pixels,-1,true); pixels.dispose();
            System.out.println("PASS: first tap, mode toggle, three restarts, input rebinding, and rendering");
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
