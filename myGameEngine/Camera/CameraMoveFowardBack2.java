package myGameEngine.Camera;
import a1.myGame;
import ray.input.action.AbstractInputAction;
import net.java.games.input.Event;
import ray.rage.scene.Camera;
import ray.rage.scene.SceneNode;
import ray.rml.Vector3;
import ray.rml.Vector3f;

public class CameraMoveFowardBack2 extends AbstractInputAction{
    private myGame game;
    private Camera camera;
    SceneNode dolphinNode;


    public CameraMoveFowardBack2(myGame g){
        game = g;
        camera = g.getEngine().getSceneManager().getCamera("MainCamera");
        dolphinNode = game.getEngine().getSceneManager().getSceneNode("dolphinTwoENode");
    }

    public void performAction(float time, Event e){

        if (e.getComponent().getName().equals("W")){
            dolphinNode.moveForward(0.1f);
        }
        else if (e.getComponent().getName().equals("S")){
            dolphinNode.moveBackward(0.1f);
        }
    }
}