define("ace/snippets/groovy",["require","exports","module"], function(require, exports, module) {
"use strict";

exports.snippetText = "# if\n\
snippet if\n\
	if (${1:true}) {\n\
		${0}\n\
	}\n\
# if ... else\n\
snippet ife\n\
	if (${1:true}) {\n\
		${2}\n\
	} else {\n\
		${0}\n\
	}\n\
# tertiary conditional\n\
snippet ter\n\
	${1:/* condition */} ? ${2:a} : ${3:b};\n\
# switch\n\
snippet switch\n\
	switch (${1:expression}) {\n\
		case '${3:case}':\n\
			${4:// code}\n\
			break;\n\
		${5}\n\
		default:\n\
			${2:// code}\n\
	}\n\
# case\n\
snippet case\n\
	case '${1:case}':\n\
		${2:// code}\n\
		break;\n\
	${3}\n\
\n\
# while (...) {...}\n\
snippet wh\n\
	while (${1:/* condition */}) {\n\
		${0:/* code */}\n\
	}\n\
# try\n\
snippet try\n\
	try {\n\
		${0:/* code */}\n\
	} catch (e) {}\n\
# do...while\n\
snippet do\n\
	do {\n\
		${2:/* code */}\n\
	} while (${1:/* condition */});\n\
# return\n\
snippet ret\n\
	return ${1:result}\n\
# for (property in object ) { ... }\n\
snippet fore\n\
	for (${1:int} ${2:elem} in ${3:Things}) {\n\
		${0:$2}\n\
	}\n\
# for (...) {...}\n\
snippet for\n\
	for (${1:int} ${2:i} = 0; $2 < ${3:Things}.length; $2++) {\n\
		${0:$3[$2]}\n\
	}\n\
# for (...) {...} (Improved Native For-Loop)\n\
snippet forr\n\
	for (${1:int} ${2:i} = ${3:Things}.length - 1; $2 >= 0; $2--) {\n\
		${0:$3[$2]}\n\
	}\n\
\n\
\n\
# CSG cube\n\
snippet cube\n\
\tCSG ${1:foo} = new Cube(${2:width}, ${3:length}, ${4:height}).toCSG();\n\
# CSG rounded cube\n\
snippet rcube\n\
\tCSG ${1:foo} = new RoundedCube(${2:width}, ${3:length}, ${4:height}).cornerRadius(${5:radius}).toCSG();\n\
# CSG cylinder\n\
snippet cyl\n\
\tCSG ${1:foo} = new Cylinder(${2:topRadius}, ${3:bottomRadius}, ${4:height}, ${5:resolution}).toCSG();\n\
# CSG sphere\n\
snippet sph\n\
\tCSG ${1:foo} = new Sphere(${2:radius}).toCSG();\n\
# CSG Polygon\n\
snippet poly\n\
\tCSG ${1:foo} = Extrude.points(new Vector3d(0, 0, ${2:50}));";
exports.scope = "groovy";

});
