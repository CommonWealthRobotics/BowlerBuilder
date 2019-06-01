

// setup threejs
var objexample = "# Group\ng v3d.csg\n\n# Vertices\nv 0.0 2.5 15.5\nv 0.0 -2.4999999999999996 15.5\nv 0.0 -2.5 10.5\nv 0.0 2.5 10.5\nv -32.0 2.5000000000000036 15.5\nv -32.0 2.500000000000004 10.5\nv -32.0 -2.4999999999999964 10.5\nv -32.0 -2.499999999999996 15.5\n\n# Faces\n\n# End Group v3d.csg\n";
var scene = new THREE.Scene();
//var loader = new THREE.STLLoader();
var loader = new THREE.OBJLoader();
var camera = new THREE.PerspectiveCamera(75, window.innerWidth / window.innerHeight, 0.1, 1000);

var renderer = new THREE.WebGLRenderer();
const material = new THREE.MeshStandardMaterial();


renderer.setSize(window.innerWidth, window.innerHeight);
document.body.appendChild(renderer.domElement);
camera.position.z = 200;
var amblight = new THREE.AmbientLight( 0x404040 ); // soft white light
scene.background = new THREE.Color( 0x8FBCD4 );
scene.add( amblight );

  // Create a directional light
  const light = new THREE.DirectionalLight( 0xffffff, 5.0 );

  // move the light back and up a bit
  light.position.set( 10, 10, 10 );

  // remember to add the light to the scene
  scene.add( light );

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
	objectLoaded(event) {}
};

var myRobot = new robot("/robots");

var updateLoop = function () {
	requestAnimationFrame(updateLoop);
				scene.rotation.x += 0.01;
				scene.rotation.y += 0.01;
	renderer.render(scene, camera);
};

// We do the async request to get the robots file.

updateLoop();
