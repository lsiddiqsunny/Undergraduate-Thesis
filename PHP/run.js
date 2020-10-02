const { Console } = require('console');
// initialize the php parser factory class
var fs = require('fs');
var engine = require('php-parser');

// initialize a new parser instance
var parser = new engine({
    parser: {
        // extractDoc: true,
        suppressErrors: true,
        php7: true
    },
    ast: {
        // withPositions: true,
        withSource: true
    },
    lexer: {
        short_tags: false,
        asp_tags: true
    }
});

for (var i = 1; i <= 320; i++) {
    try {
        for (var j = 1; j <= 3; j++) {
            var phpFile = fs.readFileSync('D:/Thesis/Undergraduate-Thesis/PHP/Dataset/Temp/' + i + '/beforeString' + j + '.txt');
            var ast = parser.parseCode('<?php\n' + phpFile);

            global.str = "";
            traverseNode(ast);
            console.log(i + " " + global.str.slice(1, -1));
            fs.writeFile('D:/Thesis/Undergraduate-Thesis/PHP/All Codes/Dataset/Temp2/' + i + '/beforeQuery' + j + '.sql', global.str.slice(1, -1), function(err, file) {
                if (err) throw err;
                console.log(i + ' Saved!');
            });
        }

    } catch (e) {
        // console.log("entering catch block");
        // console.log(e);
        // console.log("leaving catch block");
        continue;
    }



}


//console.log('File parse in json:', JSON.stringify(ast, null, 2));


function traverseNode(ast) {

    var Node = new Object();
    Node['label'] = ast.loc.source;
    Node['type'] = ast.kind;
    Node['children'] = [];
    if (Node['type'] === 'string' || Node['type'] === 'encapsed') {
        global.str = ast.loc.source;
    }
    var childNode = [];
    if (typeof ast !== 'undefined' && ast !== null) {


        if (typeof ast.children !== 'undefined' && ast.children !== null) {
            var children = ast.children;

            for (var i = 0; i < ast.children.length; i++) {
                childNode.push(traverseNode(children[i]));
            }


        }
        if (typeof ast.expression !== 'undefined' && ast.expression !== null) {
            childNode.push(traverseNode(ast.expression));
        }
        if (typeof ast.left !== 'undefined' && ast.left !== null) {
            childNode.push(traverseNode(ast.left));

        }
        if (typeof ast.right !== 'undefined' && ast.right !== null) {
            childNode.push(traverseNode(ast.right));
        }
        if (typeof ast.test !== 'undefined' && ast.test !== null) {
            childNode.push(traverseNode(ast.test));
        }
        if (typeof ast.what !== 'undefined' && ast.what !== null) {
            childNode.push(traverseNode(ast.what));
        }
        if (typeof ast.type !== 'undefined' && ast.type !== null) {
            var Node1 = new Object();
            Node1['label'] = ast.type;
            Node1['type'] = 'type';
            Node1['children'] = [];
            childNode.push(Node1);
        }
        if (typeof ast.offset !== 'undefined' && ast.offset !== null) {
            childNode.push(traverseNode(ast.offset));
        }

        if (typeof ast.body !== 'undefined' && ast.body !== null) {
            childNode.push(traverseNode(ast.body));
        }
        if (typeof ast.alternate !== 'undefined' && ast.alternate !== null) {
            childNode.push(traverseNode(ast.alternate));
        }
        if (typeof ast.expressions !== 'undefined' && ast.expressions !== null) {
            var Node1 = new Object();
            Node1['label'] = 'expressions';
            Node1['type'] = 'expressions';
            Node1['children'] = [];
            var expressions = ast.expressions;

            for (var i = 0; i < ast.expressions.length; i++) {
                Node1['children'].push(traverseNode(expressions[i]));
            }
            childNode.push(Node1);
        }

        if (typeof ast.arguments !== 'undefined' && ast.arguments !== null) {
            var Node1 = new Object();
            Node1['label'] = 'arguments';
            Node1['type'] = 'arguments';
            Node1['children'] = [];
            var arguments = ast.arguments;

            for (var i = 0; i < ast.arguments.length; i++) {
                Node1['children'].push(traverseNode(arguments[i]));
            }
            childNode.push(Node1);
        }
    }
    Node['children'] = childNode;
    return Node;

}