package myGameEngine.Camera;
import a1.*;

import net.java.games.input.Event;
import ray.input.action.AbstractInputAction;
import ray.rage.scene.Camera;
import ray.rage.scene.SceneNode;
import ray.rml.Angle;
import ray.rml.Degreef;
import ray.rml.Vector3;
import ray.rml.Vector3f;

public class CameraReset extends AbstractInputAction {

    private myGame game;
    private Camera camera;


    public CameraReset(myGame g){
        game = g;
        camera = g.getEngine().getSceneManager().getCamera("MainCamera");
    }

    @Override
    public void performAction(float v, Event event) {

        if(camera.getMode() == 'c'){

            camera.setRt((Vector3f)Vector3f.createFrom(1.0f, 0.0f, 0.0f));
            camera.setUp((Vector3f)Vector3f.createFrom(0.0f, 1.0f, 0.0f));
            camera.setFd((Vector3f)Vector3f.createFrom(0.0f, 0.0f, -1.0f));

        }
    }
}
