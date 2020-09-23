var fs = require('fs');
var JsDiff = require('diff');
var engine = require('php-parser');

for (var i = 1; i <= 80; i++)
    differencer('Dataset\\' + i + '\\before.php', 'Dataset\\' + i + '\\after.php', 'Dataset\\' + i + '\\editTree.json');

function differencer(file1, file2, file3) {

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

    var str1 = fs.readFileSync(file1, 'utf8');
    var str2 = fs.readFileSync(file2, 'utf8');

    var diff = JsDiff.diffTrimmedLines(str1, str2);
    var editTree = new Object();
    beforeCode = '';
    afterCode = '';

    var beforeNode = new Object();
    beforeNode['label'] = 'edit_root';
    beforeNode['type'] = 'edit_unit';
    beforeNode['children'] = [];

    var afterNode = new Object();
    afterNode['label'] = 'edit_root';
    afterNode['type'] = 'edit_unit';
    afterNode['children'] = [];

    diff.forEach(function(part) {
        if (part.added) {
            var eval = parser.parseEval(part.value);
            var Node = traverseNode(eval);
            afterNode['children'].push(Node);
            console.log(JSON.stringify(eval, null, 2));
            //console.log('After code:' + part.value);
            afterCode += part.value;
        } else if (part.removed) {
            var eval = parser.parseEval(part.value);
            var Node = traverseNode(eval);
            beforeNode['children'].push(Node);
            console.log(JSON.stringify(eval, null, 2));
            //console.log('Before code:' + part.value);

            beforeCode += part.value;
        }
    });
    editTree['before_code'] = beforeCode;
    editTree['after_code'] = afterCode;
    editTree['before_tree'] = beforeNode;
    editTree['after_tree'] = afterNode;
    var json = JSON.stringify(editTree, null, 2);
    //console.log(JSON.stringify(editTree, null, 2));
    fs.writeFile(file3, json, 'utf8', function(err) {
        if (err) throw err;
        console.log('complete');
    });
}

function traverseNode(ast) {

    var Node = new Object();
    Node['label'] = ast.loc.source;
    Node['type'] = ast.kind;
    Node['children'] = [];
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