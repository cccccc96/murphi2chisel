import math
import sys
import murphi
import murphiparser
import chisel
import os

def indent(s, num_space, first_line=None):
    lines = s.split('\n')
    if first_line is None:
        return '\n'.join(' ' * num_space + line for line in lines[:-1]) + "\n"
    else:
        res = ' ' * first_line + lines[0]
        if len(lines) > 1:
            res += '\n' + '\n'.join(' ' * num_space + line for line in lines[1:])
        return res


def translate_paras_top(prot: murphi.MurphiProtocol):
    paras = []
    for const in prot.consts:
        if isinstance(const, murphi.MurphiConstDecl):
            paras.append(const.name)
    return paras


def translate_io_top(prot: murphi.MurphiProtocol):
    # calc how many rules with paras -> en_a
    count = {}
    singlerule = 0
    for ruleset in prot.decls:
        if isinstance(ruleset, murphi.MurphiRuleSet):
            rule_len = len(ruleset.rule)
            factor = '*'.join(str(prot.typ_map[str(var_decl.typ.name)]) for var_decl in ruleset.var_decls)
            if factor in count:
                count[factor] = count[factor] + rule_len
            else:
                count[factor] = rule_len
        elif isinstance(ruleset, murphi.MurphiRule):
            singlerule += 1
    rule_size = '+'.join(str(count[factor]) + "*" + factor for factor in count.keys()) + "+" + str(singlerule)
    res = "val en_a = Input(UInt(log2Ceil(%s).W))\n" % rule_size

    # var -> io
    for var in prot.var_map.keys():
        value = prot.var_map[var]
        if isinstance(value, murphi.ArrayType) and isinstance(value.ele_typ, murphi.BooleanType):
            vecNum = str(prot.typ_map[str(value.idx_typ)])
            res += "val %s_out = Output(Vec(%s,Bool()))\n" % (var, vecNum)
        elif isinstance(value, murphi.ArrayType) and isinstance(prot.typ_map[str(value.ele_typ)], murphi.EnumType):
            vecNum = str(prot.typ_map[str(value.idx_typ)])
            width = math.ceil(math.log2(len(prot.typ_map[str(value.ele_typ)].names)))
            res += "val %s_out = Output(Vec(%s,UInt(%d.W)))\n" % (var, vecNum, width)
        elif isinstance(value, murphi.ArrayType) and isinstance(prot.typ_map[str(value.ele_typ)], murphi.ScalarSetType):
            vecNum = str(prot.typ_map[str(value.idx_typ)])
            width = "log2Ceil(%s)" % str(prot.typ_map[str(value.ele_typ)])
            res += "val %s_out = Output(Vec(%s,UInt(%s.W)))\n" % (var, vecNum, width)
        elif isinstance(value, murphi.ArrayType) and isinstance(prot.typ_map[str(value.ele_typ)], murphi.RecordType):
            vecNum = str(prot.typ_map[str(value.idx_typ)])
            bundle = "new %s(%s)" % (str(value.ele_typ), ','.join(const.name for const in prot.consts))
            res += "val %s_out = Output(Vec(%s,%s))\n" % (var, vecNum, bundle)

        elif isinstance(value, murphi.BooleanType):
            res += "val %s_out = Output(Bool()) \n" % (var)
        elif isinstance(value, murphi.VarType) and isinstance(prot.typ_map[str(value)], murphi.EnumType):
            width = math.ceil(math.log2(len(prot.typ_map[str(value)].names)))
            res += "val %s_out = Output(UInt(%d.W))\n" % (var, width)
        elif isinstance(value, murphi.VarType) and isinstance(prot.typ_map[str(value)], murphi.ScalarSetType):
            width = "log2Ceil(%s)" % str(prot.typ_map[str(value)])
            res += "val %s_out = Output(UInt(%s.W))\n" % (var, width)
        elif isinstance(value, murphi.VarType) and isinstance(prot.typ_map[str(value)], murphi.RecordType):
            bundle = "new %s(%s)" % (str(value), ','.join(const.name for const in prot.consts))
            res += "val %s_out = Output(%s)\n" % (var, bundle)
        # else:
        #     print("unknown!\n")
    return res


def translate_reg_top(prot: murphi.MurphiProtocol):
    res = ""
    for var in prot.var_map.keys():
        value = prot.var_map[var]
        if isinstance(value, murphi.ArrayType) and isinstance(value.ele_typ, murphi.BooleanType):
            vecNum = str(prot.typ_map[str(value.idx_typ)])
            res += "val %s_init = Wire(Vec(%s,Bool()))\n" \
                   "val %s_reg = RegInit(%s_init)\n" \
                   "io.%s_out:=%s_reg\n" \
                   % (var, vecNum, var, var, var, var)
        elif isinstance(value, murphi.ArrayType) and isinstance(prot.typ_map[str(value.ele_typ)], murphi.EnumType):
            vecNum = str(prot.typ_map[str(value.idx_typ)])
            width = math.ceil(math.log2(len(prot.typ_map[str(value.ele_typ)].names)))
            res += "val %s_init = Wire(Vec(%s,UInt(%d.W)))\n" \
                   "val %s_reg = RegInit(%s_init)\n" \
                   "io.%s_out:=%s_reg\n" \
                   % (var, vecNum, width, var, var, var, var)
        elif isinstance(value, murphi.ArrayType) and isinstance(prot.typ_map[str(value.ele_typ)], murphi.ScalarSetType):
            vecNum = str(prot.typ_map[str(value.idx_typ)])
            width = "log2Ceil(%s)" % str(prot.typ_map[str(value.ele_typ)])
            res += "val %s_init = Wire(Vec(%s,UInt(%s.W)))\n" \
                   "val %s_reg = RegInit(%s_init)\n" \
                   "io.%s_out:=%s_reg\n" \
                   % (var, vecNum, width, var, var, var, var)
        elif isinstance(value, murphi.ArrayType) and isinstance(prot.typ_map[str(value.ele_typ)], murphi.RecordType):
            vecNum = str(prot.typ_map[str(value.idx_typ)])
            bundle = "new %s(%s)" % (str(value.ele_typ), ','.join(const.name for const in prot.consts))
            res += "val %s_init = Wire(Vec(%s,%s))\n" \
                   "val %s_reg = RegInit(%s_init)\n" \
                   "io.%s_out:=%s_reg\n" \
                   % (var, vecNum, bundle, var, var, var, var)

        elif isinstance(value, murphi.BooleanType):
            res += "val %s_init = Wire(Bool())\n" \
                   "val %s_reg = RegInit(%s_init)\n" \
                   "io.%s_out:=%s_reg\n" \
                   % (var, var, var, var, var)
        elif isinstance(value, murphi.VarType) and isinstance(prot.typ_map[str(value)], murphi.EnumType):
            width = math.ceil(math.log2(len(prot.typ_map[str(value)].names)))
            res += "val %s_init = Wire(UInt(%d.W))\n" \
                   "val %s_reg = RegInit(%s_init)\n" \
                   "io.%s_out:=%s_reg\n" \
                   % (var, width, var, var, var, var)
        elif isinstance(value, murphi.VarType) and isinstance(prot.typ_map[str(value)], murphi.ScalarSetType):
            width = "log2Ceil(%s)" % str(prot.typ_map[str(value)])
            res += "val %s_init = Wire(UInt(%s.W))\n" \
                   "val %s_reg = RegInit(%s_init)\n" \
                   "io.%s_out:=%s_reg\n" \
                   % (var, width, var, var, var, var)
        elif isinstance(value, murphi.VarType) and isinstance(prot.typ_map[str(value)], murphi.RecordType):
            bundle = "new %s(%s)" % (str(value), ','.join(const.name for const in prot.consts))
            res += "val %s_init = Wire(%s)\n" \
                   "val %s_reg = RegInit(%s_init)\n" \
                   "io.%s_out:=%s_reg\n" \
                   % (var, bundle, var, var, var, var)
        # else:
        #     print("unknown!\n")
    return res


def translate_field_array_top(prot: murphi.MurphiProtocol, expr: murphi):
    res = ""
    if isinstance(expr, murphi.FieldName) and isinstance(expr.v, murphi.UnknownExpr):
        res += "%s_init.%s" % (str(expr.v), str(expr.field))
    elif isinstance(expr, murphi.FieldName):
        res += "%s.%s" % (translate_field_array_top(prot, expr.v), expr.field)
    elif isinstance(expr, murphi.ArrayIndex) and isinstance(expr.v, murphi.UnknownExpr):
        res += "%s_init(%s)" % (str(expr.v), str(expr.idx))
    elif isinstance(expr, murphi.ArrayIndex):
        res += "%s(%s)" % (translate_field_array_top(prot, expr.v), str(expr.idx))
    return res


def translate_expr_top(prot: murphi.MurphiProtocol, expr):
    res = ""
    # elaborate
    if isinstance(expr, murphi.UnknownExpr):
        if str(expr) == "true":
            expr = murphi.BooleanExpr(True)
        elif str(expr) == "false":
            expr = murphi.BooleanExpr(False)
        elif str(expr) in prot.enum_map:
            expr = murphi.EnumValExpr(prot.enum_map[str(expr)], str(expr))
        elif str(expr) in prot.var_map:
            expr = murphi.VarExpr(str(expr), prot.var_map[str(expr)])
        # else:
        #     print("unknown:" + str(expr))

    if isinstance(expr, murphi.BooleanExpr):
        res += str(expr) + ".B"
    elif isinstance(expr, murphi.EnumValExpr):
        res += "e_" + str(expr)
    elif isinstance(expr, murphi.VarExpr):
        res += str(expr) + "_init"
    elif isinstance(expr, murphi.ArrayIndex):
        res += translate_field_array_top(prot, expr)
    elif isinstance(expr, murphi.FieldName):
        res += translate_field_array_top(prot, expr)
    else:
        res += "0.U"

    return res

temp="""  Chan1_init(i).Data := 0.U
  Chan2_init(i).Data := 0.U
  Chan3_init(i).Data := 0.U
  Cache_init(i).Data := 0.U
"""

def translate_cmd_top(prot: murphi.MurphiProtocol, cmd):
    res = ""
    # IFCMD ASSIGNCMD IFCMD
    if isinstance(cmd, murphi.ForallCmd):
        iter_len = str(prot.typ_map[str(cmd.typ)])
        res += "for( %s <- 0 until %s){\n" % (cmd.var, iter_len)
        if 'german' in prot.protocolName:
            res += temp
        for subcmd in cmd.cmds:
            res += indent(translate_cmd_top(prot, subcmd),2)

        res += "}\n"
    elif isinstance(cmd, murphi.AssignCmd):
        res += "%s := %s\n" % (translate_expr_top(prot, cmd.var), translate_expr_top(prot, cmd.expr))

    # TODO: IFCMD
    # else:
    #     print("unKnown cmd")
    return res


def translate_startstate_top(prot: murphi.MurphiProtocol):
    res = "\n"
    if isinstance(prot.start_state.cmds, tuple):
        for cmd in prot.start_state.cmds:
            res += translate_cmd_top(prot, cmd)
    elif isinstance(prot.start_state.cmds.cmds, tuple):
        for cmd in prot.start_state.cmds.cmds:
            res += translate_cmd_top(prot, cmd)
    if 'german' in prot.protocolName:
        res += """MemData_init := 0.U\nAuxData_init := 0.U\nCurPtr_init := 0.U\n"""
    res += '\n'
    return res


def translate_ruleinstance_top(prot: murphi.MurphiProtocol):
    res = "var rules = ArrayBuffer[rule]()\n" \
          "var index = 0\n"
    for ruleset in prot.decls:
        if isinstance(ruleset, murphi.MurphiRuleSet):
            ind = 0
            for key in ruleset.var_map:
                var = ruleset.var_map[key]
                res += indent("for(%s <- 0 until %s) {\n" % (key, str(prot.typ_map[str(var)])),ind)
                ind+=2
            for rule in ruleset.rule:
                res += indent("rules += Module(new rule_%s(%s, %s))\n" % (rule.name, ','.join(key for key in ruleset.var_map), ','.join(const.name for const in prot.consts)),ind)
            for key in ruleset.var_map:
                ind-=2
                res += indent("}\n",ind)
            res += "\n"
        elif isinstance(ruleset, murphi.MurphiRule):
            rule = ruleset
            res += "rules += Module(new rule_%s(%s))\n" \
                   % (rule.name, ','.join(const.name for const in prot.consts))
    # io 连接
    count = {}
    singlerule = 0
    for ruleset in prot.decls:
        if isinstance(ruleset, murphi.MurphiRuleSet):
            rule_len = len(ruleset.rule)
            factor = '*'.join(str(prot.typ_map[str(var_decl.typ.name)]) for var_decl in ruleset.var_decls)
            if factor in count:
                count[factor] = count[factor] + rule_len
            else:
                count[factor] = rule_len
        elif isinstance(ruleset, murphi.MurphiRule):
            singlerule += 1
    rule_size = '+'.join(str(count[factor]) + "*" + factor for factor in count.keys()) + "+" + str(singlerule)
    res += "for(i <- 0 until %s) {\n" % rule_size
    # var_map -> n x
    for var in prot.var_map:
        res += "  rules(i).io.%s_in := %s_reg\n" % (var, var)
    res += "  rules(i).io.en_r:=(io.en_a=== index.U)\n" \
           "  when(io.en_a=== index.U){\n"
    # var_map -> n x
    for var in prot.var_map:
        res += "    %s_reg := rules(i).io.%s_out \n" % (var, var)
    res += "  }\n" \
           "  index = index +1 \n"
    res += "}\n"

    return res


def translate_asg_top(prot: murphi.MurphiProtocol):
    res = translate_reg_top(prot)
    res += translate_startstate_top(prot)
    res += translate_ruleinstance_top(prot)
    # res += translate_rule_top(prot)
    return res


def translate_topmodules(prot: murphi.MurphiProtocol, module_name):
    paras = translate_paras_top(prot)
    io = translate_io_top(prot)
    asg = translate_asg_top(prot)
    asg += translate_inv(prot)
    return chisel.Module(module_name, paras, io, asg, prot.enum_typ_map)


def translate_paras_rule(prot: murphi.MurphiProtocol, ruleset: murphi.MurphiRuleSet):
    paras = []
    for key in ruleset.var_map:
        paras.append(key)
    for const in prot.consts:
        paras.append(const.name)
    return paras


def translate_io_rule(prot: murphi.MurphiProtocol):
    # var -> io
    res = "val en_r = Input(Bool())\n"
    for var in prot.var_map.keys():
        value = prot.var_map[var]

        if isinstance(value, murphi.ArrayType) and isinstance(value.ele_typ, murphi.BooleanType):
            vecNum = str(prot.typ_map[str(value.idx_typ)])
            res += "val %s_in = Input(Vec(%s,Bool()))\n" \
                   "val %s_out = Output(Vec(%s,Bool()))\n" % (var, vecNum, var, vecNum)
        elif isinstance(value, murphi.ArrayType) and isinstance(prot.typ_map[str(value.ele_typ)], murphi.EnumType):
            vecNum = str(prot.typ_map[str(value.idx_typ)])
            width = math.ceil(math.log2(len(prot.typ_map[str(value.ele_typ)].names)))
            res += "val %s_in = Input(Vec(%s,UInt(%d.W)))\n" \
                   "val %s_out = Output(Vec(%s,UInt(%d.W)))\n" % (var, vecNum, width, var, vecNum, width)
        elif isinstance(value, murphi.ArrayType) and isinstance(prot.typ_map[str(value.ele_typ)], murphi.ScalarSetType):
            vecNum = str(prot.typ_map[str(value.idx_typ)])
            width = "log2Ceil(%s)" % str(prot.typ_map[str(value.ele_typ)])
            res += "val %s_in =Input(Vec(%s,UInt(%s.W)))\n" \
                   "val %s_out = Output(Vec(%s,UInt(%s.W)))\n" % (var, vecNum, width, var, vecNum, width)
        elif isinstance(value, murphi.ArrayType) and isinstance(prot.typ_map[str(value.ele_typ)], murphi.RecordType):
            vecNum = str(prot.typ_map[str(value.idx_typ)])
            bundle = "new %s(%s)" % (str(value.ele_typ), ','.join(const.name for const in prot.consts))
            res += "val %s_in = Input(Vec(%s,%s))\n" \
                   "val %s_out = Output(Vec(%s,%s))\n" % (var, vecNum, bundle, var, vecNum, bundle)

        elif isinstance(value, murphi.BooleanType):
            res += "val %s_in = Input(Bool()) \n" \
                   "val %s_out = Output(Bool()) \n" % (var, var)
        elif isinstance(value, murphi.VarType) and isinstance(prot.typ_map[str(value)], murphi.EnumType):
            width = math.ceil(math.log2(len(prot.typ_map[str(value)].names)))
            res += "val %s_in = Input(UInt(%d.W))\n" \
                   "val %s_out = Output(UInt(%d.W))\n" % (var, width, var, width)
        elif isinstance(value, murphi.VarType) and isinstance(prot.typ_map[str(value)], murphi.ScalarSetType):
            width = "log2Ceil(%s)" % str(prot.typ_map[str(value)])
            res += "val %s_in = Input(UInt(%s.W))\n" \
                   "val %s_out = Output(UInt(%s.W))\n" % (var, width, var, width)
        elif isinstance(value, murphi.VarType) and isinstance(prot.typ_map[str(value)], murphi.RecordType):
            bundle = "new %s(%s)" % (str(value), ','.join(const.name for const in prot.consts))
            res += "val %s_in = Input(%s)\n" \
                   "val %s_out = Output(%s)\n" % (var, bundle, var, bundle)
        # else:
        #     print("unknown!\n")

    return res


def translate_op(op):
    if op == '=':
        return "==="
    elif op == '!=':
        return "=/="
    elif op == '&':
        return "&&"
    elif op == '|':
        return "||"
    return "??"


def translate_field_array_rule_guard(prot: murphi.MurphiProtocol, expr: murphi):
    res = ""
    if isinstance(expr, murphi.FieldName) and isinstance(expr.v, murphi.UnknownExpr):
        res += "io.%s_in.%s" % (str(expr.v), str(expr.field))
    elif isinstance(expr, murphi.FieldName):
        res += "%s.%s" % (translate_field_array_rule_guard(prot, expr.v), expr.field)
    elif isinstance(expr, murphi.ArrayIndex) and isinstance(expr.v, murphi.UnknownExpr):
        res += "io.%s_in(%s)" % (str(expr.v), str(expr.idx))
    elif isinstance(expr, murphi.ArrayIndex):
        res += "%s(%s)" % (translate_field_array_rule_guard(prot, expr.v), str(expr.idx))
    return res


def translate_expr_rule_guard(prot: murphi.MurphiProtocol, expr):
    res = ""
    # elaborate
    if isinstance(expr, murphi.UnknownExpr):
        if str(expr) == "true":
            expr = murphi.BooleanExpr(True)
        elif str(expr) == "false":
            expr = murphi.BooleanExpr(False)
        elif str(expr) in prot.enum_map:
            expr = murphi.EnumValExpr(prot.enum_map[str(expr)], str(expr))
        elif str(expr) in prot.var_map:
            expr = murphi.VarExpr(str(expr), prot.var_map[str(expr)])
        # else:
        #     print("unknown")

    if isinstance(expr, murphi.BooleanExpr):
        res += str(expr) + ".B"
    elif isinstance(expr, murphi.EnumValExpr):
        res += "e_" + str(expr)
    elif isinstance(expr, murphi.VarExpr):
        res += "io.%s_in" % (str(expr))
    elif isinstance(expr, murphi.ArrayIndex):
        res += translate_field_array_rule_guard(prot, expr)
    elif isinstance(expr, murphi.FieldName):
        res += translate_field_array_rule_guard(prot, expr)
    elif isinstance(expr, murphi.OpExpr):
        if expr.op != "->":
            res += "(%s %s %s)" % (
                translate_expr_rule_guard(prot, expr.expr1), translate_op(expr.op),
                translate_expr_rule_guard(prot, expr.expr2))
        elif expr.op == "->":
            res += "(!(%s)||(%s))" % (translate_expr_rule_guard(prot, expr.expr1), translate_expr_rule_guard(prot, expr.expr2))
    elif isinstance(expr, murphi.ForallExpr):
        res += "forall_flag"
    elif isinstance(expr, murphi.NegExpr):
        res += "!(%s)" % translate_expr_rule_guard(prot, expr.expr)
    else:
        res += str(expr) + ".U"
    return res


def translate_field_array_rule_asg(prot: murphi.MurphiProtocol, expr: murphi):
    res = ""
    if isinstance(expr, murphi.FieldName) and isinstance(expr.v, murphi.UnknownExpr):
        res += "io.%s_out.%s" % (str(expr.v), str(expr.field))
    elif isinstance(expr, murphi.FieldName):
        res += "%s.%s" % (translate_field_array_rule_asg(prot, expr.v), expr.field)
    elif isinstance(expr, murphi.ArrayIndex) and isinstance(expr.v, murphi.UnknownExpr):
        res += "io.%s_out(%s)" % (str(expr.v), str(expr.idx))
    elif isinstance(expr, murphi.ArrayIndex):
        res += "%s(%s)" % (translate_field_array_rule_asg(prot, expr.v), str(expr.idx))
    return res


def translate_expr_rule_asg(prot: murphi.MurphiProtocol, expr):
    res = ""
    # elaborate
    if isinstance(expr, murphi.UnknownExpr):
        if str(expr) == "true":
            expr = murphi.BooleanExpr(True)
        elif str(expr) == "false":
            expr = murphi.BooleanExpr(False)
        elif str(expr) in prot.enum_map:
            expr = murphi.EnumValExpr(prot.enum_map[str(expr)], str(expr))
        elif str(expr) in prot.var_map:
            expr = murphi.VarExpr(str(expr), prot.var_map[str(expr)])
        # else:
        #     print("unknown")

    if isinstance(expr, murphi.BooleanExpr):
        res += str(expr) + ".B"
    elif isinstance(expr, murphi.EnumValExpr):
        res += "e_" + str(expr)
    elif isinstance(expr, murphi.VarExpr):
        res += "io.%s_out" % (str(expr))
    elif isinstance(expr, murphi.ArrayIndex):
        res += translate_field_array_rule_asg(prot, expr)
    elif isinstance(expr, murphi.FieldName):
        res += translate_field_array_rule_asg(prot, expr)
    elif isinstance(expr, murphi.OpExpr):
        res += "(%s %s %s)" % (
            translate_expr_rule_asg(prot, expr.expr1), translate_op(expr.op),
            translate_expr_rule_asg(prot, expr.expr2))
    else:
        res += str(expr) + ".U"
    return res


def translate_cmd_rule(prot: murphi.MurphiProtocol, cmd):
    res = ""
    # IFCMD ASSIGNCMD IFCMD
    if isinstance(cmd, murphi.ForallCmd):
        iter_len = str(prot.typ_map[str(cmd.typ)])
        res += "for( %s <- 0 until %s){\n" % (cmd.var, iter_len)
        for subcmd in cmd.cmds:
            res += indent(translate_cmd_rule(prot, subcmd),2)
        res += "}\n"
    elif isinstance(cmd, murphi.AssignCmd):
        res += "%s := %s\n" % (
            translate_expr_rule_asg(prot, cmd.var), translate_expr_rule_asg(prot, cmd.expr))
    elif isinstance(cmd, murphi.IfCmd):
        cond, c1 = cmd.if_branches[0]
        if len(cmd.if_branches) == 1:
            res += "when(%s){\n" % translate_expr_rule_guard(prot, cond)
            for subcmd in c1:
                res += indent(translate_cmd_rule(prot, subcmd),2)
            res += "}\n"
        if cmd.else_branch is not None:
            res += ".otherwise{\n"
            for c2 in cmd.else_branch:
                res += indent(translate_cmd_rule(prot, c2),2)
            res += "}\n"
    # else:
    #     print("unKnown cmd")
    return res


def translate_forallexpr(prot: murphi.MurphiProtocol, expr):
    res = ""
    if isinstance(expr, murphi.OpExpr):
        res += translate_forallexpr(prot, expr.expr1)
        res += translate_forallexpr(prot, expr.expr2)
    elif isinstance(expr, murphi.ForallExpr):
        res += "var forall_flag = Wire(Bool())\n" \
               "forall_flag := true.B\n" \
               "for(%s <- 0 until %s){\n" \
               "when(!(%s)){\n" \
               "  forall_flag = false.B\n" \
               "}\n}\n" \
               % (str(expr.var), prot.typ_map[str(expr.typ)], translate_expr_rule_guard(prot, expr.expr))
    return res


def translate_asg_rule(prot: murphi.MurphiProtocol, rule: murphi.MurphiRule):
    res = ""
    # for key in prot.var_map.keys():
    #     res += "io.%s_out:=io.%s_in\n" % (key, key)
    res += "when(io.en_r){\n"
    res += indent(translate_forallexpr(prot, rule.cond),2)
    res += "  when(%s){\n" % translate_expr_rule_guard(prot, rule.cond)
    for cmd in rule.cmds:
        res += indent(translate_cmd_rule(prot, cmd),4)
    res += "  }\n}\n"
    return res


def translate_rulemodules(prot: murphi.MurphiProtocol):
    modules = []
    for ruleset in prot.decls:
        if isinstance(ruleset, murphi.MurphiRuleSet):
            paras = translate_paras_rule(prot, ruleset)
            for rule in ruleset.rule:
                assert isinstance(rule, murphi.MurphiRule)
                name = "rule_%s" % rule.name
                extend = "rule(%s)" % ','.join(const.name for const in prot.consts)
                io = ""
                asg = translate_asg_rule(prot, rule)
                modules.append(chisel.Module(name, paras, io, asg, prot.enum_typ_map, extend))
        elif isinstance(ruleset, murphi.MurphiRule):
            rule = ruleset
            paras = []
            for const in prot.consts:
                paras.append(const.name)
            name = "rule_%s" % rule.name
            extend = "rule(%s)" % ','.join(const.name for const in prot.consts)
            io = ""
            asg = translate_asg_rule(prot, rule)
            modules.append(chisel.Module(name, paras, io, asg, prot.enum_typ_map, extend))

    return modules


def translate_bundles(prot: murphi.MurphiProtocol):
    bundles = []
    for key in prot.typ_map:
        type = prot.typ_map[key]
        if isinstance(type, murphi.RecordType):
            name = key

            vals = ""
            # vals: boolean array record enum const
            # TODO: boolean(done) array record(done) of val
            for subtype in type.typ_decls:
                valtype = subtype.typ
                if isinstance(subtype.typ, murphi.VarType):
                    valtype = prot.typ_map[str(subtype.typ)]

                if isinstance(valtype, murphi.ScalarSetType):
                    vals += "val %s = UInt(log2Ceil(%s).W)\n" % (subtype.name, valtype.const_name)
                elif isinstance(valtype, murphi.EnumType):
                    vals += "val %s = UInt(%d.W)\n" % (subtype.name, math.ceil(math.log2(len(valtype.names))))
                elif isinstance(valtype, murphi.BooleanType):
                    vals += "val %s = Bool()\n" % subtype.name
                elif isinstance(valtype, murphi.RecordType):
                    vals += "val %s = new %s(%s)\n" % (
                    subtype.name, str(subtype.typ), ','.join(const.name for const in prot.consts))
                elif isinstance(valtype, murphi.ArrayType):
                    vecnum = prot.typ_map[str(valtype.idx_typ)]
                    # array元素: boolean const record enum
                    t2 = valtype.ele_typ
                    if isinstance(t2, murphi.VarType):
                        t2 = prot.typ_map[str(t2)]

                    if isinstance(t2, murphi.BooleanType):
                        vals += "val %s = Vec(%s, Bool())\n" % (subtype.name, vecnum)
                    elif isinstance(t2, murphi.ScalarSetType):
                        vals += "val %s = Vec(%s, UInt(log2Ceil(%s).W))\n" % (subtype.name, vecnum, t2.const_name)
                    elif isinstance(t2, murphi.RecordType):
                        vals += "val %s = Vec(%s, new %s(%s))\n" % (
                        subtype.name, vecnum, str(valtype.ele_typ), ','.join(const.name for const in prot.consts))
                    elif isinstance(t2, murphi.EnumType):
                        vals += "val %s = Vec(%s, UInt(%d.W))\n" % (
                        subtype.name, vecnum, math.ceil(math.log2(len(t2.names))))
            # paras
            paras = []
            for const in prot.consts:
                if isinstance(const, murphi.MurphiConstDecl):
                    paras.append(const.name)
            bundles.append(chisel.Bundle(name, paras, vals))
    return bundles


def translate_abstractrule(prot: murphi.MurphiProtocol):
    name = "rule"
    extend = "Module"
    io = translate_io_rule(prot)
    asg = ""
    for key in prot.var_map.keys():
        asg += "io.%s_out:=io.%s_in\n" % (key, key)
    paras = []
    for const in prot.consts:
        paras.append(const.name)
    return chisel.Module(name, paras, io, asg, prot.enum_typ_map, extend)


def translate_forallexpr_inv(prot: murphi.MurphiProtocol, expr):
    res = ""
    if isinstance(expr, murphi.OpExpr):
        res += translate_forallexpr_inv(prot, expr.expr1)
        res += translate_forallexpr_inv(prot, expr.expr2)
    elif isinstance(expr, murphi.ForallExpr):
        res += "var forall_flag = true.B\n" \
               "for(%s <- 0 until %s){\n" \
               "when(!(%s)){\n" \
               "  forall_flag = false.B\n" \
               "}\n}\n" \
               % (str(expr.var), prot.typ_map[str(expr.typ)], translate_expr_inv(prot, expr.expr))
    return res


def translate_expr_inv(prot: murphi.MurphiProtocol, expr):
    res = ""
    # elaborate
    if isinstance(expr, murphi.UnknownExpr):
        if str(expr) == "true":
            expr = murphi.BooleanExpr(True)
        elif str(expr) == "false":
            expr = murphi.BooleanExpr(False)
        elif str(expr) in prot.enum_map:
            expr = murphi.EnumValExpr(prot.enum_map[str(expr)], str(expr))
        elif str(expr) in prot.var_map:
            expr = murphi.VarExpr(str(expr), prot.var_map[str(expr)])
        # else:
        #     print("unknown")

    if isinstance(expr, murphi.BooleanExpr):
        res += str(expr) + ".B"
    elif isinstance(expr, murphi.EnumValExpr):
        res += "e_" + str(expr)
    elif isinstance(expr, murphi.VarExpr):
        res += "%s_reg" % (str(expr))
    elif isinstance(expr, murphi.ArrayIndex):
        res += translate_field_array_inv(prot, expr)
    elif isinstance(expr, murphi.FieldName):
        res += translate_field_array_inv(prot, expr)
    elif isinstance(expr, murphi.OpExpr):
        if expr.op != "->":
            res += "(%s %s %s)" % (
                translate_expr_inv(prot, expr.expr1), translate_op(expr.op),
                translate_expr_inv(prot, expr.expr2))
        elif expr.op == "->":
            res += "(!(%s)||(%s))" % (translate_expr_inv(prot, expr.expr1), translate_expr_inv(prot, expr.expr2))
    elif isinstance(expr, murphi.ForallExpr):
        res += "forall_flag"
    elif isinstance(expr, murphi.NegExpr):
        res += "!(%s)" % translate_expr_inv(prot, expr.expr)
    else:
        res += str(expr) + ".U"
    return res

def translate_field_array_inv(prot: murphi.MurphiProtocol, expr: murphi):
    res = ""
    if isinstance(expr, murphi.FieldName) and isinstance(expr.v, murphi.UnknownExpr):
        res += "%s_reg.%s" % (str(expr.v), str(expr.field))
    elif isinstance(expr, murphi.FieldName):
        res += "%s.%s" % (translate_field_array_inv(prot, expr.v), expr.field)
    elif isinstance(expr, murphi.ArrayIndex) and isinstance(expr.v, murphi.UnknownExpr):
        res += "%s_reg(%s)" % (str(expr.v), str(expr.idx))
    elif isinstance(expr, murphi.ArrayIndex):
        res += "%s(%s)" % (translate_field_array_inv(prot, expr.v), str(expr.idx))
    return res



def translate_inv(prot: murphi.MurphiProtocol):
    res = ""
    for inv in prot.decls:
        if isinstance(inv, murphi.MurphiInvariant):
            ind=0
            if inv.var_decls is not None:
                for it in  inv.var_decls:
                    i = it.name
                    size = prot.typ_map[str(it.typ)]
                    res += indent("for (%s <- 0 until %s){\n" % (i, size), ind)
                    ind+=2

            res += indent(translate_forallexpr_inv(prot, inv.inv), ind)
            res += indent("assert(" + translate_expr_inv(prot, inv.inv) + ")\n", ind)

            if inv.var_decls is not None:
                for it in  inv.var_decls:
                    ind -= 2
                    i = it.name
                    size = prot.typ_map[str(it.typ)]
                    res += indent("}\n" , ind)

    return res



def translate_protocol(prot: murphi.MurphiProtocol, chisel_name):
    bundles = translate_bundles(prot)
    modules = []
    modules.append(translate_abstractrule(prot))
    modules += translate_rulemodules(prot)
    modules.append(translate_topmodules(prot, chisel_name))
    return chisel.Circuit(modules, bundles)


def translate_file(filename, chisel_name, out):
    prot = murphiparser.parse_file(filename)
    prot.protocolName=filename
    with open(out + chisel_name + ".scala", "w") as f:
        circuit = translate_protocol(prot, chisel_name)

        f.write(str(circuit) + '\n')





if __name__ == "__main__":
    
    filename = sys.argv[1].replace('.m','')
    src = './example/'+sys.argv[1]
    out = sys.argv[2]+'/'

    translate_file(src, filename, out)
