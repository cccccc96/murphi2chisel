module rule_Try(
  input        io_en_r,
  input  [1:0] io_n_in_0,
  input  [1:0] io_n_in_1,
  output [1:0] io_n_out_0,
  output [1:0] io_n_out_1,
  input        io_x_in,
  output       io_x_out
);
  wire [1:0] _GEN_0 = io_n_in_0 == 2'h0 ? 2'h1 : io_n_in_0; // @[mutual2.scala 26:31 mutual2.scala 27:19 mutual2.scala 18:11]
  assign io_n_out_0 = io_en_r ? _GEN_0 : io_n_in_0; // @[mutual2.scala 25:16 mutual2.scala 18:11]
  assign io_n_out_1 = io_n_in_1; // @[mutual2.scala 18:11]
  assign io_x_out = io_x_in; // @[mutual2.scala 19:11]
endmodule
module rule_Crit(
  input        io_en_r,
  input  [1:0] io_n_in_0,
  input  [1:0] io_n_in_1,
  output [1:0] io_n_out_0,
  output [1:0] io_n_out_1,
  input        io_x_in,
  output       io_x_out
);
  wire [1:0] _GEN_0 = io_n_in_0 == 2'h1 & io_x_in ? 2'h2 : io_n_in_0; // @[mutual2.scala 36:57 mutual2.scala 37:19 mutual2.scala 18:11]
  wire  _GEN_1 = io_n_in_0 == 2'h1 & io_x_in ? 1'h0 : io_x_in; // @[mutual2.scala 36:57 mutual2.scala 38:16 mutual2.scala 19:11]
  assign io_n_out_0 = io_en_r ? _GEN_0 : io_n_in_0; // @[mutual2.scala 35:16 mutual2.scala 18:11]
  assign io_n_out_1 = io_n_in_1; // @[mutual2.scala 18:11]
  assign io_x_out = io_en_r ? _GEN_1 : io_x_in; // @[mutual2.scala 35:16 mutual2.scala 19:11]
endmodule
module rule_Exit(
  input        io_en_r,
  input  [1:0] io_n_in_0,
  input  [1:0] io_n_in_1,
  output [1:0] io_n_out_0,
  output [1:0] io_n_out_1,
  input        io_x_in,
  output       io_x_out
);
  wire [1:0] _GEN_0 = io_n_in_0 == 2'h2 ? 2'h3 : io_n_in_0; // @[mutual2.scala 47:31 mutual2.scala 48:19 mutual2.scala 18:11]
  assign io_n_out_0 = io_en_r ? _GEN_0 : io_n_in_0; // @[mutual2.scala 46:16 mutual2.scala 18:11]
  assign io_n_out_1 = io_n_in_1; // @[mutual2.scala 18:11]
  assign io_x_out = io_x_in; // @[mutual2.scala 19:11]
endmodule
module rule_Idle(
  input        io_en_r,
  input  [1:0] io_n_in_0,
  input  [1:0] io_n_in_1,
  output [1:0] io_n_out_0,
  output [1:0] io_n_out_1,
  input        io_x_in,
  output       io_x_out
);
  wire [1:0] _GEN_0 = io_n_in_0 == 2'h3 ? 2'h0 : io_n_in_0; // @[mutual2.scala 57:31 mutual2.scala 58:19 mutual2.scala 18:11]
  wire  _GEN_1 = io_n_in_0 == 2'h3 | io_x_in; // @[mutual2.scala 57:31 mutual2.scala 59:16 mutual2.scala 19:11]
  assign io_n_out_0 = io_en_r ? _GEN_0 : io_n_in_0; // @[mutual2.scala 56:16 mutual2.scala 18:11]
  assign io_n_out_1 = io_n_in_1; // @[mutual2.scala 18:11]
  assign io_x_out = io_en_r ? _GEN_1 : io_x_in; // @[mutual2.scala 56:16 mutual2.scala 19:11]
endmodule
module rule_Try_1(
  input        io_en_r,
  input  [1:0] io_n_in_0,
  input  [1:0] io_n_in_1,
  output [1:0] io_n_out_0,
  output [1:0] io_n_out_1,
  input        io_x_in,
  output       io_x_out
);
  wire [1:0] _GEN_0 = io_n_in_1 == 2'h0 ? 2'h1 : io_n_in_1; // @[mutual2.scala 26:31 mutual2.scala 27:19 mutual2.scala 18:11]
  assign io_n_out_0 = io_n_in_0; // @[mutual2.scala 18:11]
  assign io_n_out_1 = io_en_r ? _GEN_0 : io_n_in_1; // @[mutual2.scala 25:16 mutual2.scala 18:11]
  assign io_x_out = io_x_in; // @[mutual2.scala 19:11]
endmodule
module rule_Crit_1(
  input        io_en_r,
  input  [1:0] io_n_in_0,
  input  [1:0] io_n_in_1,
  output [1:0] io_n_out_0,
  output [1:0] io_n_out_1,
  input        io_x_in,
  output       io_x_out
);
  wire [1:0] _GEN_0 = io_n_in_1 == 2'h1 & io_x_in ? 2'h2 : io_n_in_1; // @[mutual2.scala 36:57 mutual2.scala 37:19 mutual2.scala 18:11]
  wire  _GEN_1 = io_n_in_1 == 2'h1 & io_x_in ? 1'h0 : io_x_in; // @[mutual2.scala 36:57 mutual2.scala 38:16 mutual2.scala 19:11]
  assign io_n_out_0 = io_n_in_0; // @[mutual2.scala 18:11]
  assign io_n_out_1 = io_en_r ? _GEN_0 : io_n_in_1; // @[mutual2.scala 35:16 mutual2.scala 18:11]
  assign io_x_out = io_en_r ? _GEN_1 : io_x_in; // @[mutual2.scala 35:16 mutual2.scala 19:11]
endmodule
module rule_Exit_1(
  input        io_en_r,
  input  [1:0] io_n_in_0,
  input  [1:0] io_n_in_1,
  output [1:0] io_n_out_0,
  output [1:0] io_n_out_1,
  input        io_x_in,
  output       io_x_out
);
  wire [1:0] _GEN_0 = io_n_in_1 == 2'h2 ? 2'h3 : io_n_in_1; // @[mutual2.scala 47:31 mutual2.scala 48:19 mutual2.scala 18:11]
  assign io_n_out_0 = io_n_in_0; // @[mutual2.scala 18:11]
  assign io_n_out_1 = io_en_r ? _GEN_0 : io_n_in_1; // @[mutual2.scala 46:16 mutual2.scala 18:11]
  assign io_x_out = io_x_in; // @[mutual2.scala 19:11]
endmodule
module rule_Idle_1(
  input        io_en_r,
  input  [1:0] io_n_in_0,
  input  [1:0] io_n_in_1,
  output [1:0] io_n_out_0,
  output [1:0] io_n_out_1,
  input        io_x_in,
  output       io_x_out
);
  wire [1:0] _GEN_0 = io_n_in_1 == 2'h3 ? 2'h0 : io_n_in_1; // @[mutual2.scala 57:31 mutual2.scala 58:19 mutual2.scala 18:11]
  wire  _GEN_1 = io_n_in_1 == 2'h3 | io_x_in; // @[mutual2.scala 57:31 mutual2.scala 59:16 mutual2.scala 19:11]
  assign io_n_out_0 = io_n_in_0; // @[mutual2.scala 18:11]
  assign io_n_out_1 = io_en_r ? _GEN_0 : io_n_in_1; // @[mutual2.scala 56:16 mutual2.scala 18:11]
  assign io_x_out = io_en_r ? _GEN_1 : io_x_in; // @[mutual2.scala 56:16 mutual2.scala 19:11]
endmodule
module mutual2(
  input        clock,
  input        reset,
  input  [2:0] io_en_a,
  output [1:0] io_n_out_0,
  output [1:0] io_n_out_1,
  output       io_x_out
);
  initial begin 
    assume(reset==1);
  end

  wire  rules_0_io_en_r; // @[mutual2.scala 89:20]
  wire [1:0] rules_0_io_n_in_0; // @[mutual2.scala 89:20]
  wire [1:0] rules_0_io_n_in_1; // @[mutual2.scala 89:20]
  wire [1:0] rules_0_io_n_out_0; // @[mutual2.scala 89:20]
  wire [1:0] rules_0_io_n_out_1; // @[mutual2.scala 89:20]
  wire  rules_0_io_x_in; // @[mutual2.scala 89:20]
  wire  rules_0_io_x_out; // @[mutual2.scala 89:20]
  wire  rules_1_io_en_r; // @[mutual2.scala 90:20]
  wire [1:0] rules_1_io_n_in_0; // @[mutual2.scala 90:20]
  wire [1:0] rules_1_io_n_in_1; // @[mutual2.scala 90:20]
  wire [1:0] rules_1_io_n_out_0; // @[mutual2.scala 90:20]
  wire [1:0] rules_1_io_n_out_1; // @[mutual2.scala 90:20]
  wire  rules_1_io_x_in; // @[mutual2.scala 90:20]
  wire  rules_1_io_x_out; // @[mutual2.scala 90:20]
  wire  rules_2_io_en_r; // @[mutual2.scala 91:20]
  wire [1:0] rules_2_io_n_in_0; // @[mutual2.scala 91:20]
  wire [1:0] rules_2_io_n_in_1; // @[mutual2.scala 91:20]
  wire [1:0] rules_2_io_n_out_0; // @[mutual2.scala 91:20]
  wire [1:0] rules_2_io_n_out_1; // @[mutual2.scala 91:20]
  wire  rules_2_io_x_in; // @[mutual2.scala 91:20]
  wire  rules_2_io_x_out; // @[mutual2.scala 91:20]
  wire  rules_3_io_en_r; // @[mutual2.scala 92:20]
  wire [1:0] rules_3_io_n_in_0; // @[mutual2.scala 92:20]
  wire [1:0] rules_3_io_n_in_1; // @[mutual2.scala 92:20]
  wire [1:0] rules_3_io_n_out_0; // @[mutual2.scala 92:20]
  wire [1:0] rules_3_io_n_out_1; // @[mutual2.scala 92:20]
  wire  rules_3_io_x_in; // @[mutual2.scala 92:20]
  wire  rules_3_io_x_out; // @[mutual2.scala 92:20]
  wire  rules_4_io_en_r; // @[mutual2.scala 89:20]
  wire [1:0] rules_4_io_n_in_0; // @[mutual2.scala 89:20]
  wire [1:0] rules_4_io_n_in_1; // @[mutual2.scala 89:20]
  wire [1:0] rules_4_io_n_out_0; // @[mutual2.scala 89:20]
  wire [1:0] rules_4_io_n_out_1; // @[mutual2.scala 89:20]
  wire  rules_4_io_x_in; // @[mutual2.scala 89:20]
  wire  rules_4_io_x_out; // @[mutual2.scala 89:20]
  wire  rules_5_io_en_r; // @[mutual2.scala 90:20]
  wire [1:0] rules_5_io_n_in_0; // @[mutual2.scala 90:20]
  wire [1:0] rules_5_io_n_in_1; // @[mutual2.scala 90:20]
  wire [1:0] rules_5_io_n_out_0; // @[mutual2.scala 90:20]
  wire [1:0] rules_5_io_n_out_1; // @[mutual2.scala 90:20]
  wire  rules_5_io_x_in; // @[mutual2.scala 90:20]
  wire  rules_5_io_x_out; // @[mutual2.scala 90:20]
  wire  rules_6_io_en_r; // @[mutual2.scala 91:20]
  wire [1:0] rules_6_io_n_in_0; // @[mutual2.scala 91:20]
  wire [1:0] rules_6_io_n_in_1; // @[mutual2.scala 91:20]
  wire [1:0] rules_6_io_n_out_0; // @[mutual2.scala 91:20]
  wire [1:0] rules_6_io_n_out_1; // @[mutual2.scala 91:20]
  wire  rules_6_io_x_in; // @[mutual2.scala 91:20]
  wire  rules_6_io_x_out; // @[mutual2.scala 91:20]
  wire  rules_7_io_en_r; // @[mutual2.scala 92:20]
  wire [1:0] rules_7_io_n_in_0; // @[mutual2.scala 92:20]
  wire [1:0] rules_7_io_n_in_1; // @[mutual2.scala 92:20]
  wire [1:0] rules_7_io_n_out_0; // @[mutual2.scala 92:20]
  wire [1:0] rules_7_io_n_out_1; // @[mutual2.scala 92:20]
  wire  rules_7_io_x_in; // @[mutual2.scala 92:20]
  wire  rules_7_io_x_out; // @[mutual2.scala 92:20]
  reg [1:0] n_reg_0; // @[mutual2.scala 75:22]
  reg [1:0] n_reg_1; // @[mutual2.scala 75:22]
  reg  x_reg; // @[mutual2.scala 78:22]



  wire  _T = io_en_a == 3'h0; // @[mutual2.scala 98:31]
  wire [1:0] _GEN_0 = _T ? rules_0_io_n_out_0 : n_reg_0; // @[mutual2.scala 99:29 mutual2.scala 100:13 mutual2.scala 75:22]
  wire [1:0] _GEN_1 = _T ? rules_0_io_n_out_1 : n_reg_1; // @[mutual2.scala 99:29 mutual2.scala 100:13 mutual2.scala 75:22]
  wire  _GEN_2 = _T ? rules_0_io_x_out : x_reg; // @[mutual2.scala 99:29 mutual2.scala 101:13 mutual2.scala 78:22]
  wire  _T_2 = io_en_a == 3'h1; // @[mutual2.scala 98:31]
  wire [1:0] _GEN_3 = _T_2 ? rules_1_io_n_out_0 : _GEN_0; // @[mutual2.scala 99:29 mutual2.scala 100:13]
  wire [1:0] _GEN_4 = _T_2 ? rules_1_io_n_out_1 : _GEN_1; // @[mutual2.scala 99:29 mutual2.scala 100:13]
  wire  _GEN_5 = _T_2 ? rules_1_io_x_out : _GEN_2; // @[mutual2.scala 99:29 mutual2.scala 101:13]
  wire  _T_4 = io_en_a == 3'h2; // @[mutual2.scala 98:31]
  wire [1:0] _GEN_6 = _T_4 ? rules_2_io_n_out_0 : _GEN_3; // @[mutual2.scala 99:29 mutual2.scala 100:13]
  wire [1:0] _GEN_7 = _T_4 ? rules_2_io_n_out_1 : _GEN_4; // @[mutual2.scala 99:29 mutual2.scala 100:13]
  wire  _GEN_8 = _T_4 ? rules_2_io_x_out : _GEN_5; // @[mutual2.scala 99:29 mutual2.scala 101:13]
  wire  _T_6 = io_en_a == 3'h3; // @[mutual2.scala 98:31]
  wire [1:0] _GEN_9 = _T_6 ? rules_3_io_n_out_0 : _GEN_6; // @[mutual2.scala 99:29 mutual2.scala 100:13]
  wire [1:0] _GEN_10 = _T_6 ? rules_3_io_n_out_1 : _GEN_7; // @[mutual2.scala 99:29 mutual2.scala 100:13]
  wire  _GEN_11 = _T_6 ? rules_3_io_x_out : _GEN_8; // @[mutual2.scala 99:29 mutual2.scala 101:13]
  wire  _T_8 = io_en_a == 3'h4; // @[mutual2.scala 98:31]
  wire [1:0] _GEN_12 = _T_8 ? rules_4_io_n_out_0 : _GEN_9; // @[mutual2.scala 99:29 mutual2.scala 100:13]
  wire [1:0] _GEN_13 = _T_8 ? rules_4_io_n_out_1 : _GEN_10; // @[mutual2.scala 99:29 mutual2.scala 100:13]
  wire  _GEN_14 = _T_8 ? rules_4_io_x_out : _GEN_11; // @[mutual2.scala 99:29 mutual2.scala 101:13]
  wire  _T_10 = io_en_a == 3'h5; // @[mutual2.scala 98:31]
  wire  _GEN_17 = _T_10 ? rules_5_io_x_out : _GEN_14; // @[mutual2.scala 99:29 mutual2.scala 101:13]
  wire  _T_12 = io_en_a == 3'h6; // @[mutual2.scala 98:31]
  wire  _GEN_20 = _T_12 ? rules_6_io_x_out : _GEN_17; // @[mutual2.scala 99:29 mutual2.scala 101:13]
  wire  _T_14 = io_en_a == 3'h7; // @[mutual2.scala 98:31]
  wire  _GEN_23 = _T_14 ? rules_7_io_x_out : _GEN_20; // @[mutual2.scala 99:29 mutual2.scala 101:13]
  rule_Try rules_0 ( // @[mutual2.scala 89:20]
    .io_en_r(rules_0_io_en_r),
    .io_n_in_0(rules_0_io_n_in_0),
    .io_n_in_1(rules_0_io_n_in_1),
    .io_n_out_0(rules_0_io_n_out_0),
    .io_n_out_1(rules_0_io_n_out_1),
    .io_x_in(rules_0_io_x_in),
    .io_x_out(rules_0_io_x_out)
  );
  rule_Crit rules_1 ( // @[mutual2.scala 90:20]
    .io_en_r(rules_1_io_en_r),
    .io_n_in_0(rules_1_io_n_in_0),
    .io_n_in_1(rules_1_io_n_in_1),
    .io_n_out_0(rules_1_io_n_out_0),
    .io_n_out_1(rules_1_io_n_out_1),
    .io_x_in(rules_1_io_x_in),
    .io_x_out(rules_1_io_x_out)
  );
  rule_Exit rules_2 ( // @[mutual2.scala 91:20]
    .io_en_r(rules_2_io_en_r),
    .io_n_in_0(rules_2_io_n_in_0),
    .io_n_in_1(rules_2_io_n_in_1),
    .io_n_out_0(rules_2_io_n_out_0),
    .io_n_out_1(rules_2_io_n_out_1),
    .io_x_in(rules_2_io_x_in),
    .io_x_out(rules_2_io_x_out)
  );
  rule_Idle rules_3 ( // @[mutual2.scala 92:20]
    .io_en_r(rules_3_io_en_r),
    .io_n_in_0(rules_3_io_n_in_0),
    .io_n_in_1(rules_3_io_n_in_1),
    .io_n_out_0(rules_3_io_n_out_0),
    .io_n_out_1(rules_3_io_n_out_1),
    .io_x_in(rules_3_io_x_in),
    .io_x_out(rules_3_io_x_out)
  );
  rule_Try_1 rules_4 ( // @[mutual2.scala 89:20]
    .io_en_r(rules_4_io_en_r),
    .io_n_in_0(rules_4_io_n_in_0),
    .io_n_in_1(rules_4_io_n_in_1),
    .io_n_out_0(rules_4_io_n_out_0),
    .io_n_out_1(rules_4_io_n_out_1),
    .io_x_in(rules_4_io_x_in),
    .io_x_out(rules_4_io_x_out)
  );
  rule_Crit_1 rules_5 ( // @[mutual2.scala 90:20]
    .io_en_r(rules_5_io_en_r),
    .io_n_in_0(rules_5_io_n_in_0),
    .io_n_in_1(rules_5_io_n_in_1),
    .io_n_out_0(rules_5_io_n_out_0),
    .io_n_out_1(rules_5_io_n_out_1),
    .io_x_in(rules_5_io_x_in),
    .io_x_out(rules_5_io_x_out)
  );
  rule_Exit_1 rules_6 ( // @[mutual2.scala 91:20]
    .io_en_r(rules_6_io_en_r),
    .io_n_in_0(rules_6_io_n_in_0),
    .io_n_in_1(rules_6_io_n_in_1),
    .io_n_out_0(rules_6_io_n_out_0),
    .io_n_out_1(rules_6_io_n_out_1),
    .io_x_in(rules_6_io_x_in),
    .io_x_out(rules_6_io_x_out)
  );
  rule_Idle_1 rules_7 ( // @[mutual2.scala 92:20]
    .io_en_r(rules_7_io_en_r),
    .io_n_in_0(rules_7_io_n_in_0),
    .io_n_in_1(rules_7_io_n_in_1),
    .io_n_out_0(rules_7_io_n_out_0),
    .io_n_out_1(rules_7_io_n_out_1),
    .io_x_in(rules_7_io_x_in),
    .io_x_out(rules_7_io_x_out)
  );
  assign io_n_out_0 = n_reg_0; // @[mutual2.scala 76:11]
  assign io_n_out_1 = n_reg_1; // @[mutual2.scala 76:11]
  assign io_x_out = x_reg; // @[mutual2.scala 79:11]
  assign rules_0_io_en_r = io_en_a == 3'h0; // @[mutual2.scala 98:31]
  assign rules_0_io_n_in_0 = n_reg_0; // @[mutual2.scala 96:22]
  assign rules_0_io_n_in_1 = n_reg_1; // @[mutual2.scala 96:22]
  assign rules_0_io_x_in = x_reg; // @[mutual2.scala 97:22]
  assign rules_1_io_en_r = io_en_a == 3'h1; // @[mutual2.scala 98:31]
  assign rules_1_io_n_in_0 = n_reg_0; // @[mutual2.scala 96:22]
  assign rules_1_io_n_in_1 = n_reg_1; // @[mutual2.scala 96:22]
  assign rules_1_io_x_in = x_reg; // @[mutual2.scala 97:22]
  assign rules_2_io_en_r = io_en_a == 3'h2; // @[mutual2.scala 98:31]
  assign rules_2_io_n_in_0 = n_reg_0; // @[mutual2.scala 96:22]
  assign rules_2_io_n_in_1 = n_reg_1; // @[mutual2.scala 96:22]
  assign rules_2_io_x_in = x_reg; // @[mutual2.scala 97:22]
  assign rules_3_io_en_r = io_en_a == 3'h3; // @[mutual2.scala 98:31]
  assign rules_3_io_n_in_0 = n_reg_0; // @[mutual2.scala 96:22]
  assign rules_3_io_n_in_1 = n_reg_1; // @[mutual2.scala 96:22]
  assign rules_3_io_x_in = x_reg; // @[mutual2.scala 97:22]
  assign rules_4_io_en_r = io_en_a == 3'h4; // @[mutual2.scala 98:31]
  assign rules_4_io_n_in_0 = n_reg_0; // @[mutual2.scala 96:22]
  assign rules_4_io_n_in_1 = n_reg_1; // @[mutual2.scala 96:22]
  assign rules_4_io_x_in = x_reg; // @[mutual2.scala 97:22]
  assign rules_5_io_en_r = io_en_a == 3'h5; // @[mutual2.scala 98:31]
  assign rules_5_io_n_in_0 = n_reg_0; // @[mutual2.scala 96:22]
  assign rules_5_io_n_in_1 = n_reg_1; // @[mutual2.scala 96:22]
  assign rules_5_io_x_in = x_reg; // @[mutual2.scala 97:22]
  assign rules_6_io_en_r = io_en_a == 3'h6; // @[mutual2.scala 98:31]
  assign rules_6_io_n_in_0 = n_reg_0; // @[mutual2.scala 96:22]
  assign rules_6_io_n_in_1 = n_reg_1; // @[mutual2.scala 96:22]
  assign rules_6_io_x_in = x_reg; // @[mutual2.scala 97:22]
  assign rules_7_io_en_r = io_en_a == 3'h7; // @[mutual2.scala 98:31]
  assign rules_7_io_n_in_0 = n_reg_0; // @[mutual2.scala 96:22]
  assign rules_7_io_n_in_1 = n_reg_1; // @[mutual2.scala 96:22]
  assign rules_7_io_x_in = x_reg; // @[mutual2.scala 97:22]


  always @(posedge clock) begin
    if (reset) begin // @[mutual2.scala 75:22]
      n_reg_0 <= 2'h0; // @[mutual2.scala 75:22]
    end else if (_T_14) begin // @[mutual2.scala 99:29]
      n_reg_0 <= rules_7_io_n_out_0; // @[mutual2.scala 100:13]
    end else if (_T_12) begin // @[mutual2.scala 99:29]
      n_reg_0 <= rules_6_io_n_out_0; // @[mutual2.scala 100:13]
    end else if (_T_10) begin // @[mutual2.scala 99:29]
      n_reg_0 <= rules_5_io_n_out_0; // @[mutual2.scala 100:13]
    end else begin
      n_reg_0 <= _GEN_12;
    end
    if (reset) begin // @[mutual2.scala 75:22]
      n_reg_1 <= 2'h0; // @[mutual2.scala 75:22]
    end else if (_T_14) begin // @[mutual2.scala 99:29]
      n_reg_1 <= rules_7_io_n_out_1; // @[mutual2.scala 100:13]
    end else if (_T_12) begin // @[mutual2.scala 99:29]
      n_reg_1 <= rules_6_io_n_out_1; // @[mutual2.scala 100:13]
    end else if (_T_10) begin // @[mutual2.scala 99:29]
      n_reg_1 <= rules_5_io_n_out_1; // @[mutual2.scala 100:13]
    end else begin
      n_reg_1 <= _GEN_13;
    end
    x_reg <= reset | _GEN_23; // @[mutual2.scala 78:22 mutual2.scala 78:22]
  end



        always @(posedge clock) begin
    //
    if (~reset) begin
      assert(~(n_reg_0 == 2'h2 & n_reg_1 == 2'h2)); // @[Test.scala 113:15]
    end
    //
    if (~reset) begin
      assert(~(n_reg_1 == 2'h2 & n_reg_0 == 2'h2)); // @[Test.scala 113:15]
    end
  end


endmodule

