
class tokenizer(object):
    def __init__(self, input):
        self.input = input
        self.cur = 0
        self.alpha = 'abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ'
        self.num = '0123456789'
        self.symbols = '+-=;><'
        self.alphanum = set(self.alpha + self.num)
        self.chars = self.alpha + self.num + self.symbols

    def next_token(self):
        input = self.input
        cur = self.cur
        while cur < len(input) and input[cur] not in self.chars:
            cur += 1
        if cur == len(input):
            self.cur = cur
            return '<EOF>'

        if input[cur] in self.alpha:
            token = input[cur]
            cur += 1
            while cur < len(input) and input[cur] in self.alphanum:
                token += input[cur]
                cur += 1
            self.cur = cur
            return token
        elif input[cur] in self.num:
            token = input[cur]
            cur += 1
            while cur < len(input) and input[cur] in self.num:
                token += input[cur]
                cur += 1
            self.cur = cur
            token = int(token)
            return token
        else:
            token = input[cur]
            cur = cur + 1
            while cur < len(input) and input[cur] in self.symbols:
                token += input[cur]
                cur += 1
            self.cur = cur
            return token

if __name__ == '__main__':
    pass
