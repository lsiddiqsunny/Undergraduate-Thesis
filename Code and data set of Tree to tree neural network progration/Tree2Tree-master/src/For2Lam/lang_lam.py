import lang_for as F

class LamLang(object):
    def __init__(self):
        pass

    def serialize(self, ast):
        if type(ast) == type([]):
            if ast[0] == '<LET>':
                var = self.serialize(ast[1])
                t1 = self.serialize(ast[2])
                t2 = self.serialize(ast[3])
                return 'let %s = %s in %s' % (var, t1, t2)
            elif ast[0] == '<APP>':
                return '%s %s' % (self.serialize(ast[1]), self.serialize(ast[2]))
            elif ast[0] == '<LETREC>':
                name = self.serialize(ast[1])
                var = self.serialize(ast[2])
                t1 = self.serialize(ast[3])
                t2 = self.serialize(ast[4])
                return 'letrec %s %s = %s in %s' % (name, var, t1, t2)
            elif ast[0] == '<Expr>':
                return self.serialize(ast[1])
            elif ast[0] == '<Op+>':
                return '%s + %s' % (self.serialize(ast[1]), self.serialize(ast[2]))
            elif ast[0] == '<Op->':
                return '%s - %s' % (self.serialize(ast[1]), self.serialize(ast[2]))
            elif ast[0] == '<CMP>':
                e1 = self.serialize(ast[1])
                e2 = self.serialize(ast[3])
                s = '%s %s %s' % (e1, ast[2], e2)
                return s
            elif ast[0] == '<IF>':
                cmp = self.serialize(ast[1])
                t1 = self.serialize(ast[2])
                t2 = self.serialize(ast[3])
                return 'if %s then %s else %s' % (cmp, t1, t2)
        else:
            if ast == '<UNIT>':
                return '()'
            else:
                return str(ast)

    def translate_from_for(self, ast):
        if type(ast) == type([]):
            if ast[0] == '<SEQ>':
                t1 = self.translate_from_for(ast[1])
                t2 = self.translate_from_for(ast[2])
                if t1[0] == '<LET>' and t1[-1] == '<UNIT>':
                    t1[-1] = t2
                    return t1
                else:
                    return ['<LET>', 'blank', t1, t2]
            elif ast[0] == '<IF>':
                cmp = ast[1]
                t1 = self.translate_from_for(ast[2])
                t2 = self.translate_from_for(ast[3])
                return ['<IF>', cmp, t1, t2]
            elif ast[0] == '<FOR>':
                var = self.translate_from_for(ast[1])
                init = self.translate_from_for(ast[2])
                cmp = self.translate_from_for(ast[3])
                inc = self.translate_from_for(ast[4])
                body = self.translate_from_for(ast[5])
                tb = ['<LET>', 'blank', body, ['<APP>', 'func', inc]]
                func_body = ['<IF>', cmp, tb, '<UNIT>']
                translate = ['<LETREC>', 'func', var, func_body, ['<APP>', 'func', inc]]
                return translate
            elif ast[0] == '<ASSIGN>':
                return ['<LET>', ast[1], ast[2], '<UNIT>']
            elif ast[0] == '<Expr>':
                return ast
            elif ast[0] == '<Op+>':
                return ast
            elif ast[0] == '<Op->':
                return ast
            elif ast[0] == '<CMP>':
                return ast
        else:
            return ast


def test1():
    program = '''for i=1; i<10; i+1 do
    if x>1 then y=1 else
        y=2
        endif
    endfor
    '''
    forlang = F.ForLang()
    lamlang = LamLang()
    p = forlang.parse(program)
    ast = lamlang.translate_from_for(p)
    print(lamlang.serialize(ast))

if __name__ == '__main__':
    test1()
    pass
