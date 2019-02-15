package myGameEngine;
import a1.*;

import net.java.games.input.Event;
import ray.input.action.AbstractInputAction;
import ray.rage.scene.Camera;
import ray.rage.scene.SceneNode;
import ray.rml.Angle;
import ray.rml.Degreef;
import ray.rml.Vector3;
import ray.rml.Vector3f;

public class CameraMoveRoll extends AbstractInputAction {

    private myGame game;
    private Camera camera;
    private SceneNode dolphinCamNode;

    public CameraMoveRoll(myGame g){
        game = g;
        camera = g.getEngine().getSceneManager().getCamera("MainCamera");
        dolphinCamNode = g.getEngine().getSceneManager().getSceneNode("dolphinENode");
    }

    @Override
    public void performAction(float v, Event event) {

        Vector3 fd = camera.getFd();
        Vector3 rt = camera.getRt();
        Vector3 up = camera.getUp();
        float tilt;

        float degreeAmount = .7f;
        Angle rotAmt = Degreef.createFrom(degreeAmount);

        if(event.getValue() < -0.1 || event.getComponent().getName().equals("E") ){
            if (camera.getMode() == 'c'){
                tilt = 1f;
                Vector3 upFinal = (up.rotate(Degreef.createFrom(degreeAmount * tilt), fd)).normalize();
                Vector3 rtFinal = (rt.rotate(Degreef.createFrom(degreeAmount * tilt), fd)).normalize();
                camera.setUp((Vector3f)Vector3f.createFrom(upFinal.x(),upFinal.y(),upFinal.z()));
                camera.setRt((Vector3f)Vector3f.createFrom(rtFinal.x(),rtFinal.y(),rtFinal.z()));
            }else{
                dolphinCamNode.roll(rotAmt);
            }

        }else if (event.getValue() > 0.1 || event.getComponent().getName().equals("Q")){
            rotAmt = Degreef.createFrom(-degreeAmount);

            if (camera.getMode() == 'c'){
                tilt = -1f;
                Vector3 upFinal = (up.rotate(Degreef.createFrom(degreeAmount * tilt), fd)).normalize();
                Vector3 rtFinal = (rt.rotate(Degreef.createFrom(degreeAmount * tilt), fd)).normalize();
                camera.setUp((Vector3f)Vector3f.createFrom(upFinal.x(),upFinal.y(),upFinal.z()));
                camera.setRt((Vector3f)Vector3f.createFrom(rtFinal.x(),rtFinal.y(),rtFinal.z()));
            }else{
                dolphinCamNode.roll(rotAmt);
            }

        }

    }
}
