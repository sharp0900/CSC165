package myGameEngine.Camera;
import a1.myGame;
import ray.input.action.AbstractInputAction;
import net.java.games.input.Event;
import ray.rage.scene.Camera;
import ray.rage.scene.SceneNode;
import ray.rml.Vector3;
import ray.rml.Vector3f;

public class CameraMoveFowardBack extends AbstractInputAction{
    private myGame game;
    private Camera camera;
    SceneNode dolphinNode;
    SceneNode dolphinNodeTwo;

    public CameraMoveFowardBack(myGame g){
        game = g;
        camera = g.getEngine().getSceneManager().getCamera("MainCamera");
        dolphinNodeTwo = game.getEngine().getSceneManager().getSceneNode("dolphinENode");
    }

    public void performAction(float time, Event e){


        if ( e.getValue() <= -0.1){
                dolphinNodeTwo.moveForward(0.1f);
        }
        else if (e.getValue() >= 0.1){
                dolphinNodeTwo.moveBackward(0.1f);
        }
    }
}