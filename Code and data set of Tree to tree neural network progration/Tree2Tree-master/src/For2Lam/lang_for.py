import sys
import parser

''' Grammar:
program = statement

statement = seq | single

seq = single; seq | single; single

single = assign | if | for

assign = var = expr

var = x | y

const = 0 | 1

expr = var | const | expr + var | expr + const

if = if cmp then statement else statement endif

for = for var = expr; cmp; expr do statement endfor

cmp = expr == expr | expr > expr | expr < expr
'''

class ForLang(object):
    def parsing_error(self, tok1, tok2):
        print(self.input)
        print("Error: expecting %s but get %s at location %d" % (tok1, str(tok2), self.p.cur))
        sys.exit(1)

    def debug(self, msg):
        if self.is_debug:
            print(msg)

    def lookAHead(self):
        if self.nextToken is None:
            self.nextToken = self.p.next_token()
        return self.nextToken

    def fetch(self):
        token = self.lookAHead()
        self.nextToken = self.p.next_token()
        return token

    def parse(self, input, debug=False):
        self.is_debug = debug
        self.input = input
        self.p = parser.tokenizer(input)
        self.nextToken = None
        return self.parseProgram()

    def parseProgram(self):
        return self.parseStatement()

    def parseStatement(self):
        self.debug('parsing statement')
        s1 = self.parseSingle()
        t = self.lookAHead()
        if t == ';':
            self.fetch()
            s2 = self.parseStatement()
            return ['<SEQ>', s1, s2]
        else:
            return s1

    def parseSingle(self):
        self.debug('parsing single')
        t = self.lookAHead()
        if t == 'if':
            return self.parseIf()
        elif t == 'for':
            return self.parseFor()
        else:
            return self.parseAssign()

    def match(self, token):
        t = self.fetch()
        if t != token:
            self.parsing_error(token, t)

    def parseIf(self):
        self.debug('parsing if')
        self.match('if')
        cmp = self.parseCmp()
        self.match('then')
        then_branch = self.parseStatement()
        self.match('else')
        else_branch = self.parseStatement()
        self.match('endif')
        return ['<IF>', cmp, then_branch, else_branch]

    def parseFor(self):
        self.debug('parsing for')
        self.match('for')
        var = self.parseVar()
        self.match('=')
        init = self.parseExpr()
        self.match(';')
        cond = self.parseCmp()
        self.match(';')
        inc = self.parseExpr()
        self.match('do')
        stat = self.parseStatement()
        self.match('endfor')
        return ['<FOR>', var, init, cond, inc, stat]

    def parseAssign(self):
        self.debug('parsing assign')
        var = self.parseVar()
        self.match('=')
        expr = self.parseExpr()
        return ['<ASSIGN>', var, expr]

    def parseVar(self):
        self.debug('parsing var')
        v = self.fetch()
        if type(v) != type('a'):
            self.parsing_error('<var>', v)
        return v

    def parseConst(self):
        v = self.fetch()
        if type(v) != type(10):
            self.parsing_error('<num>', v)
        return v

    def parseExpr(self):
        self.debug('parsing expr')
        v1 = self.fetch()
        while self.lookAHead() == '+' or self.lookAHead() == '-':
            if self.lookAHead() == '+':
                self.match('+')
                v2 = self.fetch()
                v1 = ['<Op+>', v1, v2]
            else:
                self.match('-')
                v2 = self.fetch()
                v1 = ['<Op->', v1, v2]
        return ['<Expr>', v1]

    def parseCmp(self):
        self.debug('parsing cmp')
        e1 = self.parseExpr()
        op = self.lookAHead()
        if op != '==' and op != '>' and op != '<':
            self.parsing_error('==|>|<', op)
        op = self.fetch()
        e2 = self.parseExpr()
        return ['<CMP>', e1, op, e2]

    def printast(self, ast, indent):
        id = ast[0] if type(ast) == type([]) else ast
        print('\t'*indent + str(id))
        if type(ast) == type([]):
            for i in range(1, len(ast)):
                self.printast(ast[i], indent + 1)


def test1():
    program = '''for i=1; i<10; i+1 do
    if x>0 then y=y+1 else
        y=y
        endif
    endfor;
    a = a + 1
    '''
    forlang = ForLang()
    p = forlang.parse(program, debug=True)
    forlang.printast(p, 0)

if __name__ == '__main__':
    test1()
    pass


    
