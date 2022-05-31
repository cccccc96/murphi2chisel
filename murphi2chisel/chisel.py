import murphi

def indent(s, num_space, first_line=None):
    lines = s.split('\n')
    if first_line is None:
        return '\n'.join(' ' * num_space + line for line in lines[:-1]) + "\n"
    else:
        res = ' ' * first_line + lines[0]
        if len(lines) > 1:
            res += '\n' + '\n'.join(' ' * num_space + line for line in lines[1:])
        return res


class Bundle:
    def __init__(self, name, paras, vals):
        self.name = name
        self.paras = paras  # list
        self.vals = vals

    def __str__(self):
        res = "class %s(%s) extends Bundle {\n" % (self.name, ','.join(("val " + para + ":Int") for para in self.paras))
        res += indent("%s" % self.vals, 2)
        res += "}\n"
        return res


class Module:
    def __init__(self, names, paras, io, asg, enum_typ_map, extend="Module"):
        self.name = names
        self.paras = paras
        self.io = io
        self.asg = asg
        self.enum_typ_map = enum_typ_map
        self.extend = extend

    def __str__(self):
        res = "class %s(%s) extends %s{\n\n" % (
            self.name.replace(' ','_'), ','.join((para + ":Int") for para in self.paras), self.extend)
        # enum decl
        if self.extend == "Module":
            for enums in self.enum_typ_map.values():
                if isinstance(enums, murphi.EnumType):
                    res += indent("val %s::Nil = Enum(%d)\n" % (
                        '::'.join(("e_" + enumElement) for enumElement in enums.names), len(enums.names)), 2)

        # io
        res += '\n'
        if self.io != "":
            res += indent("val io = IO(new Bundle {\n", 2)
            res += indent(self.io, 4)
            res += indent("})\n\n", 2)
        res += indent(self.asg + '\n',2)

        res += '}\n'
        return res


class Circuit:
    def __init__(self, modules, bundles):
        self.modules = modules
        self.bundles = bundles

    def __str__(self):
        res = 'import chisel3._\nimport chisel3.util._\nimport scala.collection.mutable.ArrayBuffer\n'
        res += '\n'.join(str(bundle) for bundle in self.bundles)
        res += '\n'
        res += '\n'.join(str(module) for module in self.modules)
        return res
