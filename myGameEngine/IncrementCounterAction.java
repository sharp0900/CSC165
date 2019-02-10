package myGameEngine;

import a1.myGame;
import ray.input.action.AbstractInputAction;
import net.java.games.input.Event;
public class IncrementCounterAction extends AbstractInputAction
{
    private myGame game;

    public IncrementCounterAction(myGame g){
        game = g;
    }

    public void performAction(float time, Event e){
        System.out.println("counter action initiated");
        game.incrementCounter();
    }
}