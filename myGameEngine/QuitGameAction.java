package myGameEngine;

import a1.myGame;
import ray.input.action.AbstractInputAction;
import ray.rage.game.*;
import net.java.games.input.Event;
public class QuitGameAction extends AbstractInputAction
{
    private myGame game;
    public QuitGameAction(myGame g)
    {
        game = g;
    }
    public void performAction(float time, Event event)
    { System.out.println("shutdown requested");
        game.setState(Game.State.STOPPING);
    }
}