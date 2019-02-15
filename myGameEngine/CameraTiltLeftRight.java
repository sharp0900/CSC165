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

public class CameraTiltLeftRight extends AbstractInputAction {

    private myGame game;
    private Camera camera;
    private SceneNode dolphinCamNode;

    public CameraTiltLeftRight(myGame g){
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

        if(event.getValue() < -0.1 || event.getComponent().getName().equals("Left")){
            if (camera.getMode() == 'c'){
                tilt = 1f;
                Vector3 fdFinal = (fd.rotate(Degreef.createFrom(degreeAmount * tilt), up)).normalize();
                Vector3 rtFinal = (rt.rotate(Degreef.createFrom(degreeAmount * tilt), up)).normalize();
                camera.setFd((Vector3f)Vector3f.createFrom(fdFinal.x(),fdFinal.y(),fdFinal.z()));
                camera.setRt((Vector3f)Vector3f.createFrom(rtFinal.x(),rtFinal.y(),rtFinal.z()));

            }else{
                dolphinCamNode.yaw(rotAmt);
            }

        }else if (event.getValue() > 0.1 || event.getComponent().getName().equals("Right")){
            rotAmt = Degreef.createFrom(-degreeAmount);

            if (camera.getMode() == 'c'){
                tilt = -1f;
                Vector3 fdFinal = (fd.rotate(Degreef.createFrom(degreeAmount * tilt), up)).normalize();
                Vector3 rtFinal = (rt.rotate(Degreef.createFrom(degreeAmount * tilt), up)).normalize();
                camera.setFd((Vector3f)Vector3f.createFrom(fdFinal.x(),fdFinal.y(),fdFinal.z()));
                camera.setRt((Vector3f)Vector3f.createFrom(rtFinal.x(),rtFinal.y(),rtFinal.z()));
            }else{
                dolphinCamNode.yaw(rotAmt);
            }

        }

    }
}
