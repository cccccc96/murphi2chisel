import chisel3._
import chisel3.util._
import scala.collection.mutable.ArrayBuffer
class CACHE(val NODE_NUM:Int,val DATA_NUM:Int) extends Bundle {
  val State = UInt(2.W)
  val Data = UInt(log2Ceil(DATA_NUM).W)
}

class MSG(val NODE_NUM:Int,val DATA_NUM:Int) extends Bundle {
  val Cmd = UInt(3.W)
  val Data = UInt(log2Ceil(DATA_NUM).W)
}

class rule(NODE_NUM:Int,DATA_NUM:Int) extends Module{

  val e_I::e_S::e_E::Nil = Enum(3)
  val e_Empty::e_ReqS::e_ReqE::e_Inv::e_InvAck::e_GntS::e_GntE::Nil = Enum(7)

  val io = IO(new Bundle {
    val en_r = Input(Bool())
    val Cache_in = Input(Vec(NODE_NUM,new CACHE(NODE_NUM,DATA_NUM)))
    val Cache_out = Output(Vec(NODE_NUM,new CACHE(NODE_NUM,DATA_NUM)))
    val Chan1_in = Input(Vec(NODE_NUM,new MSG(NODE_NUM,DATA_NUM)))
    val Chan1_out = Output(Vec(NODE_NUM,new MSG(NODE_NUM,DATA_NUM)))
    val Chan2_in = Input(Vec(NODE_NUM,new MSG(NODE_NUM,DATA_NUM)))
    val Chan2_out = Output(Vec(NODE_NUM,new MSG(NODE_NUM,DATA_NUM)))
    val Chan3_in = Input(Vec(NODE_NUM,new MSG(NODE_NUM,DATA_NUM)))
    val Chan3_out = Output(Vec(NODE_NUM,new MSG(NODE_NUM,DATA_NUM)))
    val InvSet_in = Input(Vec(NODE_NUM,Bool()))
    val InvSet_out = Output(Vec(NODE_NUM,Bool()))
    val ShrSet_in = Input(Vec(NODE_NUM,Bool()))
    val ShrSet_out = Output(Vec(NODE_NUM,Bool()))
    val ExGntd_in = Input(Bool()) 
    val ExGntd_out = Output(Bool()) 
    val CurCmd_in = Input(UInt(3.W))
    val CurCmd_out = Output(UInt(3.W))
    val CurPtr_in = Input(UInt(log2Ceil(NODE_NUM).W))
    val CurPtr_out = Output(UInt(log2Ceil(NODE_NUM).W))
    val MemData_in = Input(UInt(log2Ceil(DATA_NUM).W))
    val MemData_out = Output(UInt(log2Ceil(DATA_NUM).W))
    val AuxData_in = Input(UInt(log2Ceil(DATA_NUM).W))
    val AuxData_out = Output(UInt(log2Ceil(DATA_NUM).W))
  )
  
  io.Cache_out:=io.Cache_in
  io.Chan1_out:=io.Chan1_in
  io.Chan2_out:=io.Chan2_in
  io.Chan3_out:=io.Chan3_in
  io.InvSet_out:=io.InvSet_in
  io.ShrSet_out:=io.ShrSet_in
  io.ExGntd_out:=io.ExGntd_in
  io.CurCmd_out:=io.CurCmd_in
  io.CurPtr_out:=io.CurPtr_in
  io.MemData_out:=io.MemData_in
  io.AuxData_out:=io.AuxData_in
  
}

class rule_Store(i:Int,d:Int,NODE_NUM:Int,DATA_NUM:Int) extends rule(NODE_NUM,DATA_NUM){


  when(io.en_r){
  
    when((io.Cache_in(i).State === e_E)){
      io.Cache_out(i).Data := d.U
      io.AuxData_out := d.U
    }
  }
  
}

class rule_SendReqS(i:Int,NODE_NUM:Int,DATA_NUM:Int) extends rule(NODE_NUM,DATA_NUM){


  when(io.en_r){
  
    when(((io.Chan1_in(i).Cmd === e_Empty) && (io.Cache_in(i).State === e_I))){
      io.Chan1_out(i).Cmd := e_ReqS
    }
  }
  
}

class rule_SendReqE(i:Int,NODE_NUM:Int,DATA_NUM:Int) extends rule(NODE_NUM,DATA_NUM){


  when(io.en_r){
  
    when(((io.Chan1_in(i).Cmd === e_Empty) && ((io.Cache_in(i).State === e_I) || (io.Cache_in(i).State === e_S)))){
      io.Chan1_out(i).Cmd := e_ReqE
    }
  }
  
}

class rule_RecvReqS(i:Int,NODE_NUM:Int,DATA_NUM:Int) extends rule(NODE_NUM,DATA_NUM){


  when(io.en_r){
  
    when(((io.CurCmd_in === e_Empty) && (io.Chan1_in(i).Cmd === e_ReqS))){
      io.CurCmd_out := e_ReqS
      io.CurPtr_out := i.U
      io.Chan1_out(i).Cmd := e_Empty
      for( j <- 0 until NODE_NUM){
        io.InvSet_out(j) := io.ShrSet_out(j)
      }
    }
  }
  
}

class rule_RecvReqE(i:Int,NODE_NUM:Int,DATA_NUM:Int) extends rule(NODE_NUM,DATA_NUM){


  when(io.en_r){
  
    when(((io.CurCmd_in === e_Empty) && (io.Chan1_in(i).Cmd === e_ReqE))){
      io.CurCmd_out := e_ReqE
      io.CurPtr_out := i.U
      io.Chan1_out(i).Cmd := e_Empty
      for( j <- 0 until NODE_NUM){
        io.InvSet_out(j) := io.ShrSet_out(j)
      }
    }
  }
  
}

class rule_SendInv(i:Int,NODE_NUM:Int,DATA_NUM:Int) extends rule(NODE_NUM,DATA_NUM){


  when(io.en_r){
  
    when(((io.Chan2_in(i).Cmd === e_Empty) && ((io.InvSet_in(i) === true.B) && ((io.CurCmd_in === e_ReqE) || ((io.CurCmd_in === e_ReqS) && (io.ExGntd_in === true.B)))))){
      io.Chan2_out(i).Cmd := e_Inv
      io.InvSet_out(i) := false.B
    }
  }
  
}

class rule_SendGntS(i:Int,NODE_NUM:Int,DATA_NUM:Int) extends rule(NODE_NUM,DATA_NUM){


  when(io.en_r){
  
    when(((io.CurCmd_in === e_ReqS) && ((io.CurPtr_in === i.U) && ((io.Chan2_in(i).Cmd === e_Empty) && (io.ExGntd_in === false.B))))){
      io.Chan2_out(i).Cmd := e_GntS
      io.Chan2_out(i).Data := io.MemData_out
      io.ShrSet_out(i) := true.B
      io.CurCmd_out := e_Empty
    }
  }
  
}

class rule_SendGntE(i:Int,NODE_NUM:Int,DATA_NUM:Int) extends rule(NODE_NUM,DATA_NUM){


  when(io.en_r){
    var forall_flag = true.B
    for(j <- 0 until NODE_NUM){
    when(!((io.ShrSet_in(j) === false.B))){
      forall_flag = false.B
    }
    }
    when(((io.CurCmd_in === e_ReqE) && ((io.CurPtr_in === i.U) && ((io.Chan2_in(i).Cmd === e_Empty) && ((io.ExGntd_in === false.B) && forall_flag))))){
      io.Chan2_out(i).Cmd := e_GntE
      io.Chan2_out(i).Data := io.MemData_out
      io.ShrSet_out(i) := true.B
      io.ExGntd_out := true.B
      io.CurCmd_out := e_Empty
    }
  }
  
}

class rule_RecvGntS(i:Int,NODE_NUM:Int,DATA_NUM:Int) extends rule(NODE_NUM,DATA_NUM){


  when(io.en_r){
  
    when((io.Chan2_in(i).Cmd === e_GntS)){
      io.Cache_out(i).State := e_S
      io.Cache_out(i).Data := io.Chan2_out(i).Data
      io.Chan2_out(i).Cmd := e_Empty
    }
  }
  
}

class rule_RecvGntE(i:Int,NODE_NUM:Int,DATA_NUM:Int) extends rule(NODE_NUM,DATA_NUM){


  when(io.en_r){
  
    when((io.Chan2_in(i).Cmd === e_GntE)){
      io.Cache_out(i).State := e_E
      io.Cache_out(i).Data := io.Chan2_out(i).Data
      io.Chan2_out(i).Cmd := e_Empty
    }
  }
  
}

class rule_SendInvAck(i:Int,NODE_NUM:Int,DATA_NUM:Int) extends rule(NODE_NUM,DATA_NUM){


  when(io.en_r){
  
    when(((io.Chan2_in(i).Cmd === e_Inv) && (io.Chan3_in(i).Cmd === e_Empty))){
      io.Chan2_out(i).Cmd := e_Empty
      io.Chan3_out(i).Cmd := e_InvAck
      when((io.Cache_in(i).State === e_E)){
        io.Chan3_out(i).Data := io.Cache_out(i).Data
      }
      io.Cache_out(i).State := e_I
    }
  }
  
}

class rule_RecvInvAck(i:Int,NODE_NUM:Int,DATA_NUM:Int) extends rule(NODE_NUM,DATA_NUM){


  when(io.en_r){
  
    when(((io.Chan3_in(i).Cmd === e_InvAck) && (io.CurCmd_in =/= e_Empty))){
      io.Chan3_out(i).Cmd := e_Empty
      io.ShrSet_out(i) := false.B
      when((io.ExGntd_in === true.B)){
        io.ExGntd_out := false.B
        io.MemData_out := io.Chan3_out(i).Data
      }
    }
  }
  
}

class german(NODE_NUM:Int,DATA_NUM:Int) extends Module{

  val e_I::e_S::e_E::Nil = Enum(3)
  val e_Empty::e_ReqS::e_ReqE::e_Inv::e_InvAck::e_GntS::e_GntE::Nil = Enum(7)

  val io = IO(new Bundle {
    val en_a = Input(UInt(log2Ceil(1*NODE_NUM*DATA_NUM+11*NODE_NUM+0).W))
    val Cache_out = Output(Vec(NODE_NUM,new CACHE(NODE_NUM,DATA_NUM)))
    val Chan1_out = Output(Vec(NODE_NUM,new MSG(NODE_NUM,DATA_NUM)))
    val Chan2_out = Output(Vec(NODE_NUM,new MSG(NODE_NUM,DATA_NUM)))
    val Chan3_out = Output(Vec(NODE_NUM,new MSG(NODE_NUM,DATA_NUM)))
    val InvSet_out = Output(Vec(NODE_NUM,Bool()))
    val ShrSet_out = Output(Vec(NODE_NUM,Bool()))
    val ExGntd_out = Output(Bool()) 
    val CurCmd_out = Output(UInt(3.W))
    val CurPtr_out = Output(UInt(log2Ceil(NODE_NUM).W))
    val MemData_out = Output(UInt(log2Ceil(DATA_NUM).W))
    val AuxData_out = Output(UInt(log2Ceil(DATA_NUM).W))
  )
  
  val Cache_init = Wire(Vec(NODE_NUM,new CACHE(NODE_NUM,DATA_NUM)))
  val Cache_reg = RegInit(Cache_init)
  io.Cache_out:=Cache_reg
  val Chan1_init = Wire(Vec(NODE_NUM,new MSG(NODE_NUM,DATA_NUM)))
  val Chan1_reg = RegInit(Chan1_init)
  io.Chan1_out:=Chan1_reg
  val Chan2_init = Wire(Vec(NODE_NUM,new MSG(NODE_NUM,DATA_NUM)))
  val Chan2_reg = RegInit(Chan2_init)
  io.Chan2_out:=Chan2_reg
  val Chan3_init = Wire(Vec(NODE_NUM,new MSG(NODE_NUM,DATA_NUM)))
  val Chan3_reg = RegInit(Chan3_init)
  io.Chan3_out:=Chan3_reg
  val InvSet_init = Wire(Vec(NODE_NUM,Bool()))
  val InvSet_reg = RegInit(InvSet_init)
  io.InvSet_out:=InvSet_reg
  val ShrSet_init = Wire(Vec(NODE_NUM,Bool()))
  val ShrSet_reg = RegInit(ShrSet_init)
  io.ShrSet_out:=ShrSet_reg
  val ExGntd_init = Wire(Bool())
  val ExGntd_reg = RegInit(ExGntd_init)
  io.ExGntd_out:=ExGntd_reg
  val CurCmd_init = Wire(UInt(3.W))
  val CurCmd_reg = RegInit(CurCmd_init)
  io.CurCmd_out:=CurCmd_reg
  val CurPtr_init = Wire(UInt(log2Ceil(NODE_NUM).W))
  val CurPtr_reg = RegInit(CurPtr_init)
  io.CurPtr_out:=CurPtr_reg
  val MemData_init = Wire(UInt(log2Ceil(DATA_NUM).W))
  val MemData_reg = RegInit(MemData_init)
  io.MemData_out:=MemData_reg
  val AuxData_init = Wire(UInt(log2Ceil(DATA_NUM).W))
  val AuxData_reg = RegInit(AuxData_init)
  io.AuxData_out:=AuxData_reg
  
  for( i <- 0 until NODE_NUM){
    Chan1_init(i).Cmd := e_Empty
    Chan2_init(i).Cmd := e_Empty
    Chan3_init(i).Cmd := e_Empty
    Cache_init(i).State := e_I
    InvSet_init(i) := false.B
    ShrSet_init(i) := false.B
  }
  ExGntd_init := false.B
  CurCmd_init := e_Empty
  MemData_init := 0.U
  AuxData_init := 0.U
  
  var rules = ArrayBuffer[rule]()
  var index = 0
  for(i <- 0 until NODE_NUM) {
    for(d <- 0 until DATA_NUM) {
      rules += Module(new rule_Store(i,d, NODE_NUM,DATA_NUM))
    }
  }
  
  for(i <- 0 until NODE_NUM) {
    rules += Module(new rule_SendReqS(i, NODE_NUM,DATA_NUM))
  }
  
  for(i <- 0 until NODE_NUM) {
    rules += Module(new rule_SendReqE(i, NODE_NUM,DATA_NUM))
  }
  
  for(i <- 0 until NODE_NUM) {
    rules += Module(new rule_RecvReqS(i, NODE_NUM,DATA_NUM))
  }
  
  for(i <- 0 until NODE_NUM) {
    rules += Module(new rule_RecvReqE(i, NODE_NUM,DATA_NUM))
  }
  
  for(i <- 0 until NODE_NUM) {
    rules += Module(new rule_SendInv(i, NODE_NUM,DATA_NUM))
  }
  
  for(i <- 0 until NODE_NUM) {
    rules += Module(new rule_SendGntS(i, NODE_NUM,DATA_NUM))
  }
  
  for(i <- 0 until NODE_NUM) {
    rules += Module(new rule_SendGntE(i, NODE_NUM,DATA_NUM))
  }
  
  for(i <- 0 until NODE_NUM) {
    rules += Module(new rule_RecvGntS(i, NODE_NUM,DATA_NUM))
  }
  
  for(i <- 0 until NODE_NUM) {
    rules += Module(new rule_RecvGntE(i, NODE_NUM,DATA_NUM))
  }
  
  for(i <- 0 until NODE_NUM) {
    rules += Module(new rule_SendInvAck(i, NODE_NUM,DATA_NUM))
  }
  
  for(i <- 0 until NODE_NUM) {
    rules += Module(new rule_RecvInvAck(i, NODE_NUM,DATA_NUM))
  }
  
  for(i <- 0 until 1*NODE_NUM*DATA_NUM+11*NODE_NUM+0) {
    rules(i).io.Cache_in := Cache_reg
    rules(i).io.Chan1_in := Chan1_reg
    rules(i).io.Chan2_in := Chan2_reg
    rules(i).io.Chan3_in := Chan3_reg
    rules(i).io.InvSet_in := InvSet_reg
    rules(i).io.ShrSet_in := ShrSet_reg
    rules(i).io.ExGntd_in := ExGntd_reg
    rules(i).io.CurCmd_in := CurCmd_reg
    rules(i).io.CurPtr_in := CurPtr_reg
    rules(i).io.MemData_in := MemData_reg
    rules(i).io.AuxData_in := AuxData_reg
    rules(i).io.en_r:=(io.en_a=== index.U)
    when(io.en_a=== index.U){
      Cache_reg := rules(i).io.Cache_out 
      Chan1_reg := rules(i).io.Chan1_out 
      Chan2_reg := rules(i).io.Chan2_out 
      Chan3_reg := rules(i).io.Chan3_out 
      InvSet_reg := rules(i).io.InvSet_out 
      ShrSet_reg := rules(i).io.ShrSet_out 
      ExGntd_reg := rules(i).io.ExGntd_out 
      CurCmd_reg := rules(i).io.CurCmd_out 
      CurPtr_reg := rules(i).io.CurPtr_out 
      MemData_reg := rules(i).io.MemData_out 
      AuxData_reg := rules(i).io.AuxData_out 
    }
    index = index +1 
  }
  for (i <- 0 until NODE_NUM){
    for (j <- 0 until NODE_NUM){
  
      assert((!((i.U =/= j.U))||(((!((Cache_reg(i).State === e_E))||((Cache_reg(j).State === e_I))) && (!((Cache_reg(i).State === e_S))||(((Cache_reg(j).State === e_I) || (Cache_reg(j).State === e_S))))))))
    }
  }
  var forall_flag = true.B
  for(i <- 0 until NODE_NUM){
  when(!((!((Cache_reg(i).State =/= e_I))||((Cache_reg(i).Data === AuxData_reg))))){
    forall_flag = false.B
  }
  }
  assert(((!((ExGntd_reg === false.B))||((MemData_reg === AuxData_reg))) && forall_flag))
  
}

