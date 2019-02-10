package myGameEngine;
import a1.myGame;
import ray.input.action.AbstractInputAction;
import net.java.games.input.Event;
import ray.rage.scene.Camera;

public class CameraChangeView extends AbstractInputAction{
    private myGame game;
    private Camera camera;

    public CameraChangeView(myGame g){
        game = g;
        camera = g.getEngine().getSceneManager().getCamera("MainCamera");
    }

    public void performAction(float time, Event e){
        System.out.println("Camera position has now changed.");

        if(camera.getMode() == 'c'){
            camera.setMode('n');
            game.cameraOnDolphin(camera);
        }else {
            camera.setMode('c');
            game.cameraOffDolphin(camera);
        }
    }
}
