package a1;



import com.jogamp.newt.event.MouseListener;
import myGameEngine.*;

import java.awt.*;
import java.awt.event.MouseEvent;

import java.awt.event.MouseMotionListener;
import java.io.*;

import myGameEngine.Camera.*;
import myGameEngine.NodeController.TeleportController;
import net.java.games.input.Component;
import net.java.games.input.Controller;
import ray.rage.*;
import ray.rage.game.*;
import ray.rage.rendersystem.*;
import ray.rage.rendersystem.Renderable.*;
import ray.rage.rendersystem.shader.GpuShaderProgram;
import ray.rage.scene.*;
import ray.rage.scene.Camera.Frustum.*;
import ray.rage.scene.controllers.*;
import ray.rage.util.BufferUtil;
import ray.rml.*;
import ray.rage.rendersystem.gl4.GL4RenderSystem;
import ray.rage.rendersystem.states.*;
import ray.rage.asset.texture.*;
import ray.input.*;

import javax.swing.*;

import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.util.ArrayList;
import java.util.Random;

public class myGame extends VariableFrameRateGame  implements
        MouseListener, MouseMotionListener {

    // to minimize variable allocation in update()
    GL4RenderSystem rs;
    GenericInputManager im;
    float elapsTime = 0.0f, timeCounter = 0.0f;
    boolean stop = false;
    String elapsTimeStr, counterStr, dispStr;
    int elapsTimeSec, counter = 0, maxPlanets = 5, muberPoints = 0, muberTotal = 0, centerX = 0, centerY = 0;
    SceneNode nullNode;

    private Camera3Pcontroller orbitController1, orbitController2 ;
    private Robot robot; // these are additional variable declarations
    private Canvas canvas;
    private RenderWindow rw;
    private float prevMouseX, prevMouseY, curMouseX, curMouseY;
    private boolean isRecentering;


    // Array to hold planets
    SceneNode[] planetAmount = new SceneNode[maxPlanets];
    // Array to hold planets that have been visited.
    SceneNode[] planetVisited = new SceneNode[maxPlanets];
    SceneNode[] planetVisitedTwo = new SceneNode[maxPlanets];

    String[] textureFiles = {"blue.jpeg","chain-fence.jpeg", "earth-day.jpeg","earth-night.jpeg","hexagons.jpeg", "moon.jpeg", "red.jpeg"};

    public myGame() {
        super();
    }

    public static void main(String[] args) {
        Game game = new myGame();
        try {
            game.startup();
            game.run();
        } catch (Exception e) {
            e.printStackTrace(System.err);
        } finally {
            game.shutdown();
            game.exit();
        }
    }

    @Override
    protected void setupWindow(RenderSystem rs, GraphicsEnvironment ge) {
        rs.createRenderWindow(new DisplayMode(1000, 700, 24, 60), false);
    }

    @Override
    protected void setupCameras(SceneManager sm, RenderWindow rw) {
        SceneNode rootNode = sm.getRootSceneNode();
        RenderSystem rs = sm.getRenderSystem();

        // Camera 1
        Camera camera = sm.createCamera("MainCamera", Projection.PERSPECTIVE);
        rw.getViewport(0).setCamera(camera);
        SceneNode cameraN =
                rootNode.createChildSceneNode("MainCameraNode");
        cameraN.attachObject(camera);
        camera.setMode('n');
        camera.getFrustum().setFarClipDistance(1000.0f);

        // Camera 2
        Camera camera2 = sm.createCamera("MainCamera2",
                Projection.PERSPECTIVE);
        rw.getViewport(1).setCamera(camera2);
        SceneNode cameraN2 =
                rootNode.createChildSceneNode("MainCamera2Node");
        cameraN2.attachObject(camera2);
        camera2.setMode('n');
        camera2.getFrustum().setFarClipDistance(1000.0f);

        initMouseMode(rs, rw);
    }

    protected void setupWindowViewports(RenderWindow rw)
    { rw.addKeyListener(this);
        Viewport topViewport = rw.getViewport(0);
        topViewport.setDimensions(.51f, .01f, .99f, .49f); // B,L,W,H
        topViewport.setClearColor(new Color(0.0f, .7f, .7f));

        Viewport botViewport = rw.createViewport(.01f, .01f, .99f, .49f);
        botViewport.setClearColor(new Color(.5f, 1.0f, .5f));
    }

    @Override
    protected void setupScene(Engine eng, SceneManager sm) throws IOException {

        /*========= Creating a Null Test Node ==================================================== */
        nullNode = sm.getRootSceneNode().createChildSceneNode("NULL");
        /*======================================================================================== */

        /*========= Objects to set up planets ==================================================== */
        Entity[] planetE = new Entity[maxPlanets];
        int maxDistance = 20;
        int minDistance = 2;
        /*=========================================================================================*/

        /*========= Objects to set up changing textures ==================================================== */
        TextureManager tm = eng.getTextureManager();
        RenderSystem rs = sm.getRenderSystem();
        TextureState state;
        /*===================================================================================================*/

        /*========= DOLPHIN and DOLPHIN CAMERA NODE ==================================================== */
        Entity dolphinE = sm.createEntity("myDolphin", "dolphinHighPoly.obj");
        dolphinE.setPrimitive(Primitive.TRIANGLES);

        SceneNode dolphinN = sm.getRootSceneNode().createChildSceneNode("dolphinENode");
        dolphinN.moveBackward(2.0f);
        dolphinN.attachObject(dolphinE);

        /*=======================================================================*/

        //========== DOLPHIN SECONED PLAYER AND CAMERA =====================================//

        Entity dolphinTwoE = sm.createEntity("myDolphinTwo", "dolphinHighPoly.obj");
        dolphinE.setPrimitive(Primitive.TRIANGLES);

        SceneNode dolphinTwoN = sm.getRootSceneNode().createChildSceneNode("dolphinTwoENode");
        dolphinTwoN.moveBackward(2.0f);
        dolphinTwoN.attachObject(dolphinTwoE);

        //=======================================================================//

        /*========= PLANETS ==================================================== */
        for (int i = 0; i < maxPlanets; i++){
            planetE[i] = sm.createEntity("myPlanet" + i, "earth.obj");
            planetE[i].setPrimitive(Primitive.TRIANGLES);
        }

        for (int i = 0; i < maxPlanets; i++){
            float scalePlanetNum = new Random().nextFloat() + 0.5f;
            planetAmount[i] = sm.getRootSceneNode().createChildSceneNode(planetE[i].getName() + "Node");
            planetAmount[i].moveBackward((float)new Random().nextInt((maxDistance - minDistance) + 1) + minDistance);
            planetAmount[i].moveForward((float)new Random().nextInt((maxDistance - minDistance) + 1) + minDistance);
            planetAmount[i].moveLeft((float)new Random().nextInt((maxDistance - minDistance) + 1) + minDistance);
            planetAmount[i].moveRight((float)new Random().nextInt((maxDistance - minDistance) + 1) + minDistance);
            planetAmount[i].scale(scalePlanetNum,scalePlanetNum,scalePlanetNum);
            planetAmount[i].attachObject(planetE[i]);
        }
        /*=======================================================================*/

        /*========= Code to spawn the ground ==================================================== */
        SceneObject xBarE = makeXBarEngine(eng,sm);
        ((ManualObject) xBarE).setPrimitive(Primitive.TRIANGLES);
        SceneNode xBarN = sm.getRootSceneNode().createChildSceneNode("XBar");
        xBarN.scale(200.0f,200.0f,200.0f);
        xBarN.attachObject(xBarE);
        /*=======================================================================*/

        /*======== LIGHTING ====================================================*/
        sm.getAmbientLight().setIntensity(new Color(.1f, .1f, .1f));

        Light plight = sm.createLight("testLamp1", Light.Type.POINT);
        plight.setAmbient(new Color(.3f, .3f, .3f));
        plight.setDiffuse(new Color(.7f, .7f, .7f));
        plight.setSpecular(new Color(1.0f, 1.0f, 1.0f));
        plight.setRange(5f);

        SceneNode plightNode = sm.getRootSceneNode().createChildSceneNode("plightNode");
        plightNode.attachObject(plight);
        /*=======================================================================*/

        /*======== ROTATION and Texture Set ====================================================*/
        //RotationController rc = new RotationController(Vector3f.createUnitVectorY(), .02f);
        for (int i = 0; i < maxPlanets; i++){
            state = (TextureState) rs.createRenderState(RenderState.Type.TEXTURE);
            //rc.addNode(planetAmount[i]);
            state.setTexture(tm.getAssetByPath(textureFiles[new Random().nextInt(textureFiles.length)]));
            planetE[i].setRenderState(state);
        }
        //sm.addController(rc);
        /*=======================================================================*/

        state = (TextureState) rs.createRenderState(RenderState.Type.TEXTURE);
        state.setTexture(tm.getAssetByPath("Dolphin_HighPolyUV_Muber.jpg"));
        dolphinE.setRenderState(state);

        //====== This will setup the Orbit Camera ================================//
        setupOrbitCamera(eng, sm);
        dolphinN.yaw(Degreef.createFrom(45.0f));
        dolphinTwoN.yaw(Degreef.createFrom(45.0f));

        // This will call a function that will create the inputs for the game.
        setupInputs();

    }

    protected void setupOrbitCamera(Engine eng, SceneManager sm) {
        im = new GenericInputManager();
        String gpName = im.getFirstGamepadName();
        SceneNode dolphinN = sm.getSceneNode("dolphinENode");
        SceneNode cameraN = sm.getSceneNode("MainCameraNode");
        Camera camera = sm.getCamera("MainCamera");
        orbitController1 =
                new Camera3Pcontroller(camera, cameraN, dolphinN, gpName, im);

        SceneNode dolphinTwoN = sm.getSceneNode("dolphinTwoENode");
        SceneNode cameraTwoN = sm.getSceneNode("MainCamera2Node");
        Camera camera2 = sm.getCamera("MainCamera2");
        String gpName2 = im.getMouseName();
        orbitController2 =
                new Camera3Pcontroller(camera2, cameraTwoN, dolphinTwoN, gpName2, im);
    }

    protected void setupInputs(){
        // build some action objects for doing things in response to user input
        QuitGameAction quitGameAction = new QuitGameAction(this);
        CameraMoveFowardBack cameraMoveFoward = new CameraMoveFowardBack(this);
        CameraMoveLeftRight cameraMoveLeftRight = new CameraMoveLeftRight(this);
        CameraMoveFowardBack2 cameraMoveFowardTwo = new CameraMoveFowardBack2(this);
        CameraMoveLeftRight2 cameraMoveLeftRightTwo = new CameraMoveLeftRight2(this);
        CameraTiltLeftRight cameraTiltLeftRight = new CameraTiltLeftRight(this);
        CameraTiltUpDown cameraTiltUpDown = new CameraTiltUpDown(this);
        CameraReset cameraReset = new CameraReset(this);
        CameraMoveRoll cameraMoveRoll = new CameraMoveRoll(this);

        // Creates and sets up inputs.
        //im = new GenericInputManager();
        ArrayList controllers = im.getControllers();
        for (int i = 0; i < controllers.size(); i++) {
            Controller c = (Controller)controllers.get(i);
            if (c.getType() == Controller.Type.KEYBOARD) {
                im.associateAction(
                        c,
                        Component.Identifier.Key.W,
                        new CameraMoveFowardBack2(this),
                        InputManager.INPUT_ACTION_TYPE.REPEAT_WHILE_DOWN);
                im.associateAction(
                        c,
                        Component.Identifier.Key.S,
                        new CameraMoveFowardBack2(this),
                        InputManager.INPUT_ACTION_TYPE.REPEAT_WHILE_DOWN);
                im.associateAction(
                        c,
                        Component.Identifier.Key.D,
                        new CameraMoveLeftRight2(this),
                        InputManager.INPUT_ACTION_TYPE.REPEAT_WHILE_DOWN);
                im.associateAction(
                        c,
                        Component.Identifier.Key.A,
                        new CameraMoveLeftRight2(this),
                        InputManager.INPUT_ACTION_TYPE.REPEAT_WHILE_DOWN);

                im.associateAction(
                        c,
                        Component.Identifier.Key.SPACE,
                        new CameraChangeView(this),
                        InputManager.INPUT_ACTION_TYPE.ON_PRESS_ONLY);

                im.associateAction(
                        c,
                        Component.Identifier.Key.R,
                        new CameraReset(this),
                        InputManager.INPUT_ACTION_TYPE.REPEAT_WHILE_DOWN);

                im.associateAction(
                        c,
                        Component.Identifier.Key.E,
                        new CameraMoveRoll(this),
                        InputManager.INPUT_ACTION_TYPE.REPEAT_WHILE_DOWN);

                im.associateAction(
                        c,
                        Component.Identifier.Key.Q,
                        new CameraMoveRoll(this),
                        InputManager.INPUT_ACTION_TYPE.REPEAT_WHILE_DOWN);

            }
        else if (c.getType() == Controller.Type.GAMEPAD || c.getType() == Controller.Type.STICK) {

            im.associateAction(c,
                    net.java.games.input.Component.Identifier.Button._9,
                    quitGameAction,
                    InputManager.INPUT_ACTION_TYPE.ON_PRESS_ONLY);
//            im.associateAction(c,
//                    net.java.games.input.Component.Identifier.Button._3,
//                    cameraReset,
//                    InputManager.INPUT_ACTION_TYPE.ON_PRESS_ONLY);
            im.associateAction(c,
                    Component.Identifier.Axis.Y,
                    cameraMoveFoward,
                    InputManager.INPUT_ACTION_TYPE.REPEAT_WHILE_DOWN);
            im.associateAction(c,
                    Component.Identifier.Axis.X,
                    cameraMoveLeftRight,
                    InputManager.INPUT_ACTION_TYPE.REPEAT_WHILE_DOWN);
//            im.associateAction(c,
//                    Component.Identifier.Axis.RX,
//                    cameraTiltLeftRight,
//                    InputManager.INPUT_ACTION_TYPE.REPEAT_WHILE_DOWN);
//            im.associateAction(c,
//                    Component.Identifier.Axis.RY,
//                    cameraTiltUpDown,
//                    InputManager.INPUT_ACTION_TYPE.REPEAT_WHILE_DOWN);
//            im.associateAction(c,
//                    Component.Identifier.Axis.Z,
//                    cameraMoveRoll,
//                    InputManager.INPUT_ACTION_TYPE.REPEAT_WHILE_DOWN);
            }
        }
    }

    //============== UPDATE ==========================================================================
    @Override
    protected void update(Engine engine) {
        // build and set HUD
        rs = (GL4RenderSystem) engine.getRenderSystem();
        elapsTime += engine.getElapsedTimeMillis();
        elapsTimeSec = Math.round(elapsTime/1000.0f);
        elapsTimeStr = Integer.toString(elapsTimeSec);
        counterStr = Integer.toString(counter);
        im.update(elapsTime);
        checkDistance();
        checkDistanceTwo();
        orbitController1.updateCameraPosition();
        orbitController2.updateCameraPosition();
        dispStr = hudContent("Time = " + elapsTimeSec +"  Visited Planets = " + counterStr + "   Muber Points = " + muberTotal );
        rs.setHUD(dispStr, 13, 13);
        rs.setHUD2(dispStr, 15, (rs.getRenderWindow().getViewport(0).getActualHeight() + 25));
    }
    //==============================================================================================

    // ======== This will update the HUD when the player gets too far from the dolphin. ===============
    private String hudContent(String display){
        String content = display;
        if(stop && getEngine().getSceneManager().getCamera("MainCamera").getMode() == 'n'){
            content = "  Too far from dolphin, Press B(Controller) or SpaceBar(Keyboard)." ;
        }else if (muberPoints != 0){
            content = content + "     Nice drop off, you just got rated a " +  muberPoints + " out of 5!";
        }
        return content;
    }
    //==========================================================================================

    // ======== This will check the distant between the player and the Dolphin. ===============
    public boolean dolphinDistanceLimit(){
        boolean limit = true;
        float distanceLimit = 3.0f;
        float distanceX, distanceZ;
        SceneNode dolphin = getEngine().getSceneManager().getSceneNode("dolphinENode");
        Vector3 cameraPosition = getEngine().getSceneManager().getCamera("MainCamera").getPo();

        distanceX = Math.abs(dolphin.getLocalPosition().x() - cameraPosition.x());
        distanceZ = Math.abs(dolphin.getLocalPosition().z() - cameraPosition.z());

        if (distanceX > distanceLimit && distanceZ > distanceLimit){
            limit = false;
            stop = true;
        }else {
            stop = false;
        }

        return limit;
    }
    //==========================================================================================

    //======== This will check the distant between the player and the planetsa ================
    private void checkDistance(){
        if(getEngine().getSceneManager().getCamera("MainCamera").getMode() == 'n'){
            Vector3 planetPosition;
            float distanceX, distanceZ,distantLimit = 1.5f;
            SceneNode dolphin = getEngine().getSceneManager().getSceneNode("dolphinENode");

            for (int i = 0; i < maxPlanets; i++){
                planetPosition = planetAmount[i].getLocalPosition();
                if (!(visitYet(planetAmount[i]))){
                    distanceX = Math.abs(dolphin.getLocalPosition().x() - planetPosition.x());
                    distanceZ = Math.abs(dolphin.getLocalPosition().z() - planetPosition.z());
                    if(distanceX < distantLimit && distanceZ < distantLimit){
                        planetVisited[i] = planetAmount[i];
                        //incrementCounter();
                        RotationController rc = new RotationController(Vector3f.createUnitVectorY(), .02f);
                        rc.addNode(planetAmount[i]);
                        getEngine().getSceneManager().addController(rc);
                        }
                    }
                }
            }
    }


    private void checkDistanceTwo(){
        if(getEngine().getSceneManager().getCamera("MainCamera2").getMode() == 'n'){
            Vector3 planetPosition;
            float distanceX, distanceZ,distantLimit = 1.5f;
            SceneNode dolphin = getEngine().getSceneManager().getSceneNode("dolphinTwoENode");

            for (int i = 0; i < maxPlanets; i++){
                planetPosition = planetAmount[i].getLocalPosition();
                if (!(visitYet(planetAmount[i]))){
                    distanceX = Math.abs(dolphin.getLocalPosition().x() - planetPosition.x());
                    distanceZ = Math.abs(dolphin.getLocalPosition().z() - planetPosition.z());
                    if(distanceX < distantLimit && distanceZ < distantLimit){
                        planetVisitedTwo[i] = planetAmount[i];
                        incrementCounter();
                        TeleportController tc = new TeleportController(getEngine().getSceneManager().getSceneNode("dolphinENode"));
                        tc.addNode(planetAmount[i]);
                        getEngine().getSceneManager().addController(tc);
                    }
                }
            }
        }
    }
    //==========================================================================================

    //======== This will check if a planet has been visited yet =======================
    private boolean visitYet(SceneNode nodePlanet){
        boolean isIn = false;
        for (int i=0; i < maxPlanets; i++){
            if (nodePlanet == planetVisited[i] || nodePlanet == planetVisitedTwo[i]){
                isIn = true;
            }
        }
        return isIn;
    }
    //==========================================================================================

    //=========== This will create X Cordanate Bar as a manual object =========================
    private ManualObject makeXBarEngine (Engine eng, SceneManager sm) throws IOException {
        ManualObject xBar = sm.createManualObject("XBar");
        ManualObjectSection xBarSec = xBar.createManualSection("SquareSection");
        xBar.setGpuShaderProgram(sm.getRenderSystem().
                getGpuShaderProgram(GpuShaderProgram.Type.RENDERING));

        float[] vertices = new float[]{
                -1.0f, -0.01f, 1.0f, 1.0f,  -0.01f, 1.0f, -1.0f,   -0.01f, -1.0f,
                -1.0f,  -0.01f, -1.0f, 1.0f,   -0.01f, 1.0f, -1.0f,    -0.01f, 1.0f, //UF
                1.0f,    -0.01f, -1.0f, -1.0f,   -0.01f, -1.0f, 1.0f,    -0.01f, 1.0f,
                1.0f,    -0.01f, 1.0f, -1.0f,    -0.01f, -1.0f, 1.0f,    -0.01f, -1.0f //UR
        };

        float[] texcoords = new float[]{
                0.0f, 0.0f, 1.0f,0.0f, 0.0f, 1.0f,
                0.0f, 0.0f, 1.0f,0.0f, 0.0f, 1.0f,
                0.0f, 0.0f, 1.0f,0.0f, 0.0f, 1.0f,
                0.0f, 0.0f, 1.0f,0.0f, 0.0f, 1.0f,
        };

        float[] normals = new float[]{
                0.0f, 1.0f, 0.0f, 0.0f, 1.0f, 0.0f,
                0.0f, 0.0f, 0.0f, 0.0f, 1.0f, 0.0f,
                0.0f, 1.0f, 0.0f, 0.0f, 0.0f, 0.0f,
                0.0f, 1.0f, 0.0f, 0.0f, 1.0f, 0.0f

        };

        int[] indices = new int[] {0,1,2,3,4,5,6,7,8,9,10,11};

        FloatBuffer vertBuf = BufferUtil.directFloatBuffer(vertices);
        FloatBuffer texBuf = BufferUtil.directFloatBuffer(texcoords);
        FloatBuffer normBuf = BufferUtil.directFloatBuffer(normals);
        IntBuffer indexBuf = BufferUtil.directIntBuffer(indices);

        xBarSec.setVertexBuffer(vertBuf);
        xBarSec.setTextureCoordsBuffer(texBuf);
        xBarSec.setNormalsBuffer(normBuf);
        xBarSec.setIndexBuffer(indexBuf);

        Texture tex =
                eng.getTextureManager().getAssetByPath("moon.jpeg");
        TextureState texState = (TextureState)sm.getRenderSystem().
                createRenderState(RenderState.Type.TEXTURE);
        texState.setTexture(tex);
        FrontFaceState faceState = (FrontFaceState) sm.getRenderSystem().
                createRenderState(RenderState.Type.FRONT_FACE);

        xBar.setDataSource(DataSource.INDEX_BUFFER);
        xBar.setRenderState(texState);
        xBar.setRenderState(faceState);

        return xBar;
    }
    //==========================================================================================

    //=============== Game Logic Ends ====================================================

    public void incrementCounter() {
        counter++;
    }

    //============== Mouse Movement =====================================================

    private void initMouseMode(RenderSystem s, RenderWindow w) {
         rw = w;
         rs = (GL4RenderSystem) s;
        Viewport v = rw.getViewport(1);
        int left = rw.getLocationLeft();
        int top = rw.getLocationTop();
        int widt = v.getActualScissorWidth();
        int hei = v.getActualScissorHeight();
         centerX = left + widt / 2;
         centerY = top + hei / 2;
        isRecentering = false;
        try // note that some platforms may not support the Robot class
        {
            robot = new Robot();
        } catch (AWTException ex) {
            throw new RuntimeException("Couldn't create Robot!");
        }
        recenterMouse();
        prevMouseX = centerX; // 'prevMouse' defines the initial
        prevMouseY = centerY; // mouse position
        // also change the cursor
        Image faceImage = new
                ImageIcon("./assets/images/face.gif").getImage();
        Cursor faceCursor = Toolkit.getDefaultToolkit().
                createCustomCursor(faceImage, new Point(0,0), "FaceCursor");
        canvas = rs.getCanvas();
        canvas.setCursor(faceCursor);
    }

    private void recenterMouse()
    {// use the robot to move the mouse to the center point.
// Note that this generates one MouseEvent.
        Viewport v = rw.getViewport(1);
        int left = rw.getLocationLeft();
        int top = rw.getLocationTop();
        int widt = v.getActualScissorWidth();
        int hei = v.getActualScissorHeight();
         centerX = left + widt / 2;
         centerY = top + hei / 2;
        isRecentering = true;
        canvas = rs.getCanvas();
        robot.mouseMove(centerX,centerY);
    }

    @Override
    public void mouseClicked(com.jogamp.newt.event.MouseEvent mouseEvent) {

    }

    @Override
    public void mouseEntered(com.jogamp.newt.event.MouseEvent mouseEvent) {

    }

    @Override
    public void mouseExited(com.jogamp.newt.event.MouseEvent mouseEvent) {

    }

    @Override
    public void mousePressed(com.jogamp.newt.event.MouseEvent mouseEvent) {

    }

    @Override
    public void mouseReleased(com.jogamp.newt.event.MouseEvent mouseEvent) {

    }

    @Override
    public void mouseMoved(com.jogamp.newt.event.MouseEvent e) {
        System.out.println("HERRRRRRRRRRRRRRRRRRRRRRRRRRE");

        if (isRecentering &&
                centerX == e.getX() && centerY == e.getY())
        { isRecentering = false; } // mouse recentered, recentering complete
        else
        { // event was due to a user mouse-move, and must be processed
            curMouseX = e.getX();
            curMouseY = e.getY();
            float mouseDeltaX = prevMouseX - curMouseX;
            float mouseDeltaY = prevMouseY - curMouseY;
//            yaw(mouseDeltaX);
//            pitch(mouseDeltaY);
            prevMouseX = curMouseX;
            prevMouseY = curMouseY;
            System.out.println(curMouseX);
            System.out.println(curMouseY);
// tell robot to put the cursor to the center (since user just moved it)
            recenterMouse();
            prevMouseX = centerX; //reset prev to center
            prevMouseY = centerY;
        }
    }

    @Override
    public void mouseDragged(com.jogamp.newt.event.MouseEvent mouseEvent) {

    }

    @Override
    public void mouseWheelMoved(com.jogamp.newt.event.MouseEvent mouseEvent) {

    }


    //============== Mouse Movement END =====================================================
}
