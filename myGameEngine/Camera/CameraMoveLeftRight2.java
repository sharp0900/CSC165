package myGameEngine.Camera;

import a1.myGame;
import ray.input.action.AbstractInputAction;
import net.java.games.input.Event;
import ray.rage.scene.Camera;
import ray.rage.scene.SceneNode;
import ray.rml.Vector3;
import ray.rml.Vector3f;

public class CameraMoveLeftRight2 extends AbstractInputAction{
    private myGame game;
    private Camera camera;
    SceneNode dolphinNode;


    public CameraMoveLeftRight2(myGame g){
        game = g;
        camera = g.getEngine().getSceneManager().getCamera("MainCamera");
        dolphinNode = game.getEngine().getSceneManager().getSceneNode("dolphinTwoENode");
    }

    public void performAction(float time, Event e){
        if (e.getComponent().getName().equals("A")){
            dolphinNode.moveRight(0.1f);
        }
        else if (e.getComponent().getName().equals("D")){
            dolphinNode.moveLeft(0.1f);
        }
    }
}