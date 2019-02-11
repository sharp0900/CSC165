package myGameEngine;
import a1.myGame;
import ray.input.action.AbstractInputAction;
import net.java.games.input.Event;
import ray.rage.scene.Camera;
import ray.rage.scene.SceneNode;
import ray.rml.Vector3f;

public class CameraChangeView extends AbstractInputAction{
    private myGame game;
    private Camera camera;
    private SceneNode nodeDolphin;
    private SceneNode nodeCamearaDolphin;

    public CameraChangeView(myGame g){
        game = g;
        nodeDolphin = g.getEngine().getSceneManager().getSceneNode("dolphinENode");
        nodeCamearaDolphin =  g.getEngine().getSceneManager().getSceneNode("dolphinCameraNode");
        camera = g.getEngine().getSceneManager().getCamera("MainCamera");
    }

    public void performAction(float time, Event e){
        System.out.println("Camera position has now changed.");

        if(camera.getMode() == 'c'){
            camera.setMode('n');
        }else {
           camera.setMode('c');
           camera.setPo((Vector3f) nodeDolphin.getLocalPosition().add(-0.5f, 0.0f , 0.0f));
        }
    }
}
