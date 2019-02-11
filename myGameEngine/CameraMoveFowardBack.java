package myGameEngine;
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

    public CameraMoveFowardBack(myGame g){
        game = g;
        camera = g.getEngine().getSceneManager().getCamera("MainCamera");
        dolphinNode = game.getEngine().getSceneManager().getSceneNode("dolphinENode");
    }

    public void performAction(float time, Event e){

        if(camera.getMode() == 'c'){
            Vector3f viewVector = camera.getFd();
            Vector3f currentPosition = camera.getPo();

            Vector3f pMatrixMultiply =
                    (Vector3f) Vector3f.createFrom(0.01f*viewVector.x(), 0.01f*viewVector.y(), 0.01f*viewVector.z());
            Vector3f newPosition = (Vector3f) currentPosition.add(pMatrixMultiply);

            camera.setPo((Vector3f)Vector3f.createFrom(newPosition.x(),newPosition.y(),newPosition.z()));
            System.out.println("Camera has moved foward.");
        }
        else{
            dolphinNode.moveForward(0.1f);
            System.out.println("Camera has moved foward.");
        }
    }
}