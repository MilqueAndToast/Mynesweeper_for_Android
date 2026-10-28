package com.tjstudios.mynesweeper;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Files;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.utils.BaseDrawable;
import com.badlogic.gdx.scenes.scene2d.utils.Drawable;
import org.junit.Test;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import static org.junit.Assert.*;

public class GameplayTest {
    private void put(Object obj, String name, Object value) throws Exception {
        Field f = Mynesweeper.class.getDeclaredField(name); f.setAccessible(true); f.set(obj, value);
    }
    private Object get(Object obj, String name) throws Exception {
        Field f = Mynesweeper.class.getDeclaredField(name); f.setAccessible(true); return f.get(obj);
    }
    private Object call(Object obj, String name, Class<?>[] types, Object... args) throws Exception {
        Method m = Mynesweeper.class.getDeclaredMethod(name, types); m.setAccessible(true); return m.invoke(obj, args);
    }
    private Mynesweeper board(int w, int h, int... mines) throws Exception {
        Gdx.files = (Files)java.lang.reflect.Proxy.newProxyInstance(Files.class.getClassLoader(),
            new Class<?>[]{Files.class}, (proxy, method, args) -> null);
        Mynesweeper g = new Mynesweeper();
        put(g, "gridWidth", w); put(g, "gridHeight", h); put(g, "bombCount", mines.length);
        int[][] values = new int[w][h];
        for(int i : mines) values[i % w][i / w] = 9;
        put(g, "mineVals", values);
        for(int y=0;y<h;y++) for(int x=0;x<w;x++) if(values[x][y]!=9)
            values[x][y]=(Integer)call(g,"bombCheck",new Class<?>[]{int.class,int.class},x,y);
        BitmapFont font = new BitmapFont(new BitmapFont.BitmapFontData(), new TextureRegion(), false);
        TextButton.TextButtonStyle style = new TextButton.TextButtonStyle(); style.font=font;
        ArrayList<TextButton> tiles = new ArrayList<TextButton>();
        for(int i=0;i<w*h;i++) { TextButton t=new TextButton("",new TextButton.TextButtonStyle(style)); t.setName("hidden"); tiles.add(t); }
        put(g,"mineField",tiles);
        put(g,"mineSkin",new Skin() {
            @Override public Drawable newDrawable(String name, Color tint) { return new BaseDrawable(); }
        });
        return g;
    }
    @SuppressWarnings("unchecked") private ArrayList<TextButton> tiles(Mynesweeper g) throws Exception {
        return (ArrayList<TextButton>)get(g,"mineField");
    }
    private void reveal(Mynesweeper g,int i) throws Exception {
        tiles(g).get(i).setName("revealed"); put(g,"numRevealed",(Integer)get(g,"numRevealed")+1);
        call(g,"gridProcess",new Class<?>[]{});
    }
    @Test public void zeroExpansionWinsWithoutFlags() throws Exception {
        Mynesweeper g=board(4,4,15); reveal(g,0);
        assertEquals(15,get(g,"numRevealed")); assertEquals(true,get(g,"winCheck"));
        assertEquals("hidden",tiles(g).get(15).getName());
        call(g,"gridProcess",new Class<?>[]{}); assertEquals(15,get(g,"numRevealed"));
    }
    @Test public void expansionPreservesFlags() throws Exception {
        Mynesweeper g=board(4,4,15); tiles(g).get(1).setName("flag"); reveal(g,0);
        assertEquals("flag",tiles(g).get(1).getName()); assertEquals(14,get(g,"numRevealed"));
        assertEquals(false,get(g,"winCheck"));
    }
    @Test public void firstRevealRelocatesMineAndRecounts() throws Exception {
        Mynesweeper g=board(3,3,0,8);
        call(g,"makeFirstRevealSafe",new Class<?>[]{int.class},0);
        int[][] v=(int[][])get(g,"mineVals"); int count=0;
        assertTrue(v[0][0]!=9);
        for(int y=0;y<3;y++)for(int x=0;x<3;x++) {
            if(v[x][y]==9) count++;
            else assertEquals(call(g,"bombCheck",new Class<?>[]{int.class,int.class},x,y),v[x][y]);
        }
        assertEquals(2,count);
    }
    @Test public void revealingMineLoses() throws Exception {
        Mynesweeper g=board(3,3,8); reveal(g,8);
        assertEquals(true,get(g,"lossCheck")); assertEquals(false,get(g,"winCheck"));
    }
    @Test public void diagonalCluesAndCorners() throws Exception {
        Mynesweeper g=board(3,3,0,4,8); int[][] v=(int[][])get(g,"mineVals");
        assertEquals(2,v[1][0]); assertEquals(1,v[2][0]); assertEquals(2,v[2][1]);
    }
}
