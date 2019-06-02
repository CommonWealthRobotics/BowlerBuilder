class webSocketHandler {
	ws = null;
	robot = null;
	constructor(robot, wsuri) {
		var obj = this;
		this.robot = robot;
		this.ws = new WebSocket(wsuri);
		this.ws.binaryType = 'arraybuffer';
		this.ws.onmessage = function (j) {
			obj.handleData(j);
		};
	}
	connect(wsconnect) {
		console.log("Websocket Open");
	}

	handleData(e) {
		console.log("web-socket-handler: Got Data!" + e.data);
		// data is little endian
		var dv = new DataView(e.data);
		var command = dv.getUint32(0);
		// console.log("command: " + command);
		switch (command) {
		case 1:
			// Position Update
			this.positionUpdate(dv);
			break;
		case 2:
			// Dummy Command


			break;
		}
	}
	positionUpdate(dv) {

		var transform = [];
		var rlink = dv.getUint32(4)
			for (var i = 0; i < 16; i++) {
				transform.push(dv.getFloat32((i + 2) * 4));
			}
			var m = new THREE.Matrix4();
		m.elements = transform;
		//debugger;
		this.robot.linkObjects[rlink].transform = m;
		this.robot.linkObjects[rlink].update=true;
		// console.log("web-socket-handler: Position for link " + rlink + "! " + transform);

	}
	dummyCommand(dv) {
		console.log("Dummy Command!");
	}
}

// robot object
class robotLink {
	constructor(uri, index) {
		var obj = this;
		console.log("display-links: New Link '" + uri + "': " + index + "");
		// Load OBJ and register our selves as callback
		loader.load(uri, function (j) {
			obj.addToScene(j);
		});

		this.index = index;
	}
	index = null;
	sceneobject = null;
	transform = null;
	update=false;
	addToScene(mesh) {
		console.log("display-links: Object " + this.index + " loaded");

		//debugger;
		//debugger;
		// Set a material
		//mesh.children[0].material = new THREE.MeshBasicMaterial( { color: 0x00ff00 } );
		this.sceneobject = mesh;
		//debugger;
		// Add ourselves to the global scene.
		scene.add(mesh);
	}
}

class robot {
	// an array of robotLinks for future reference.
	linkObjects = [];
	constructor(uri) {
		var obj = this;
		console.log("display-links: New Robot '" + uri + "'");
		//kick off async request for robots file.
		$.getJSON(uri, function (j) {
			// register our function as a callback upon completion
			obj.loadCad(j);
		});
		this.linkObjects = [];
	};
	loadCad(json) {
		// Request returned with valid JSON
		var cad = json.robots[0].cad;
		console.log("display-links: Loading " + cad.length + " CAD objects");

		// Iterate over create robotLink Objects
		for (var i = 0; i < cad.length; i++) {
			console.log("display-links: Adding Link " + i);
			// Pass URI and index to the constructor.
			var robotlink = new robotLink(cad[i], i);
			this.linkObjects.push(robotlink);

		}
	}
	applyTransforms() {
		var lojb = this.linkObjects;
		
		for (var i = 0; i < lojb.length; i++) {
			if (lojb[i].transform != null && lojb[i].sceneobject != null && lojb[i].update) {
				// console.log("display-links: Updating matrix");
				lojb[i].sceneobject.applyMatrix(lojb[i].transform);
				lojb[i].update=false;
			}
		}
	}
	};

		var scene = new THREE.Scene();
	//var loader = new THREE.STLLoader();
	var loader = new THREE.OBJLoader();
	var camera = new THREE.PerspectiveCamera(75, window.innerWidth / window.innerHeight, 0.1, 1000);

	var renderer = new THREE.WebGLRenderer();
	const material = new THREE.MeshStandardMaterial();

	renderer.setSize(window.innerWidth, window.innerHeight);
	document.body.appendChild(renderer.domElement);
	camera.position.z = 200;
	var amblight = new THREE.AmbientLight(0x404040); // soft white light
	scene.background = new THREE.Color(0x8FBCD4);
	scene.add(amblight);

	// Create a directional light
	const light = new THREE.DirectionalLight(0xffffff, 5.0);

	// move the light back and up a bit
	light.position.set(10, 10, 10);

	// remember to add the light to the scene
	scene.add(light);

	var myRobot = new robot("/robots");
	let wsuri = ((window.location.protocol === "https:") ? "wss://" : "ws://") + window.location.host + "/robot/socket/MyTestRobot";
	var wshandle = new webSocketHandler(myRobot, wsuri);

	var updateLoop = function () {
		myRobot.applyTransforms()
		requestAnimationFrame(updateLoop);
		scene.rotation.x += 0.01;
		scene.rotation.y += 0.01;
		renderer.render(scene, camera);
	};

	// We do the async request to get the robots file.

	updateLoop();
