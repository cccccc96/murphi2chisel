import chisel3._
import chisel3.util._
import scala.collection.mutable.ArrayBuffer
class NODE_STATE(val NODE_NUM:Int,val DATA_NUM:Int) extends Bundle {
  val ProcCmd = UInt(2.W)
  val InvMarked = Bool()
  val CacheState = UInt(2.W)
  val CacheData = UInt(log2Ceil(DATA_NUM).W)
}

class DIR_STATE(val NODE_NUM:Int,val DATA_NUM:Int) extends Bundle {
  val Pending = Bool()
  val Local = Bool()
  val Dirty = Bool()
  val HeadVld = Bool()
  val HeadPtr = UInt(log2Ceil(NODE_NUM).W)
  val HomeHeadPtr = Bool()
  val ShrVld = Bool()
  val ShrSet = Vec(NODE_NUM, Bool())
  val HomeShrSet = Bool()
  val InvSet = Vec(NODE_NUM, Bool())
  val HomeInvSet = Bool()
}

class UNI_MSG(val NODE_NUM:Int,val DATA_NUM:Int) extends Bundle {
  val Cmd = UInt(3.W)
  val Proc = UInt(log2Ceil(NODE_NUM).W)
  val HomeProc = Bool()
  val Data = UInt(log2Ceil(DATA_NUM).W)
}

class INV_MSG(val NODE_NUM:Int,val DATA_NUM:Int) extends Bundle {
  val Cmd = UInt(2.W)
}

class RP_MSG(val NODE_NUM:Int,val DATA_NUM:Int) extends Bundle {
  val Cmd = UInt(1.W)
}

class WB_MSG(val NODE_NUM:Int,val DATA_NUM:Int) extends Bundle {
  val Cmd = UInt(1.W)
  val Proc = UInt(log2Ceil(NODE_NUM).W)
  val HomeProc = Bool()
  val Data = UInt(log2Ceil(DATA_NUM).W)
}

class SHWB_MSG(val NODE_NUM:Int,val DATA_NUM:Int) extends Bundle {
  val Cmd = UInt(2.W)
  val Proc = UInt(log2Ceil(NODE_NUM).W)
  val HomeProc = Bool()
  val Data = UInt(log2Ceil(DATA_NUM).W)
}

class NAKC_MSG(val NODE_NUM:Int,val DATA_NUM:Int) extends Bundle {
  val Cmd = UInt(1.W)
}

class STATE(val NODE_NUM:Int,val DATA_NUM:Int) extends Bundle {
  val Proc = Vec(NODE_NUM, new NODE_STATE(NODE_NUM,DATA_NUM))
  val HomeProc = new NODE_STATE(NODE_NUM,DATA_NUM)
  val Dir = new DIR_STATE(NODE_NUM,DATA_NUM)
  val MemData = UInt(log2Ceil(DATA_NUM).W)
  val UniMsg = Vec(NODE_NUM, new UNI_MSG(NODE_NUM,DATA_NUM))
  val HomeUniMsg = new UNI_MSG(NODE_NUM,DATA_NUM)
  val InvMsg = Vec(NODE_NUM, new INV_MSG(NODE_NUM,DATA_NUM))
  val HomeInvMsg = new INV_MSG(NODE_NUM,DATA_NUM)
  val RpMsg = Vec(NODE_NUM, new RP_MSG(NODE_NUM,DATA_NUM))
  val HomeRpMsg = new RP_MSG(NODE_NUM,DATA_NUM)
  val WbMsg = new WB_MSG(NODE_NUM,DATA_NUM)
  val ShWbMsg = new SHWB_MSG(NODE_NUM,DATA_NUM)
  val NakcMsg = new NAKC_MSG(NODE_NUM,DATA_NUM)
  val CurrData = UInt(log2Ceil(DATA_NUM).W)
}

class rule(NODE_NUM:Int,DATA_NUM:Int) extends Module{

  val e_CACHE_I::e_CACHE_S::e_CACHE_E::Nil = Enum(3)
  val e_NODE_None::e_NODE_Get::e_NODE_GetX::Nil = Enum(3)
  val e_UNI_None::e_UNI_Get::e_UNI_GetX::e_UNI_Put::e_UNI_PutX::e_UNI_Nak::Nil = Enum(6)
  val e_INV_None::e_INV_Inv::e_INV_InvAck::Nil = Enum(3)
  val e_RP_None::e_RP_Replace::Nil = Enum(2)
  val e_WB_None::e_WB_Wb::Nil = Enum(2)
  val e_SHWB_None::e_SHWB_ShWb::e_SHWB_FAck::Nil = Enum(3)
  val e_NAKC_None::e_NAKC_Nakc::Nil = Enum(2)

  val io = IO(new Bundle {
    val en_r = Input(Bool())
    val Sta_in = Input(new STATE(NODE_NUM,DATA_NUM))
    val Sta_out = Output(new STATE(NODE_NUM,DATA_NUM))
  )
  
  io.Sta_out:=io.Sta_in
  
}

class rule_Store(src:Int,data:Int,NODE_NUM:Int,DATA_NUM:Int) extends rule(NODE_NUM,DATA_NUM){


  when(io.en_r){
  
    when((io.Sta_in.Proc(src).CacheState === e_CACHE_E)){
      io.Sta_out.Proc(src).CacheData := data.U
      io.Sta_out.CurrData := data.U
    }
  }
  
}

class rule_Store_Home(data:Int,NODE_NUM:Int,DATA_NUM:Int) extends rule(NODE_NUM,DATA_NUM){


  when(io.en_r){
  
    when((io.Sta_in.HomeProc.CacheState === e_CACHE_E)){
      io.Sta_out.HomeProc.CacheData := data.U
      io.Sta_out.CurrData := data.U
    }
  }
  
}

class rule_PI_Remote_Get(src:Int,NODE_NUM:Int,DATA_NUM:Int) extends rule(NODE_NUM,DATA_NUM){


  when(io.en_r){
  
    when(((io.Sta_in.Proc(src).ProcCmd === e_NODE_None) && (io.Sta_in.Proc(src).CacheState === e_CACHE_I))){
      io.Sta_out.Proc(src).ProcCmd := e_NODE_Get
      io.Sta_out.UniMsg(src).Cmd := e_UNI_Get
      io.Sta_out.UniMsg(src).HomeProc := true.B
    }
  }
  
}

class rule_PI_Local_Get_Get(NODE_NUM:Int,DATA_NUM:Int) extends rule(NODE_NUM,DATA_NUM){


  when(io.en_r){
  
    when(((io.Sta_in.HomeProc.ProcCmd === e_NODE_None) && ((io.Sta_in.HomeProc.CacheState === e_CACHE_I) && (!(io.Sta_in.Dir.Pending) && io.Sta_in.Dir.Dirty)))){
      io.Sta_out.HomeProc.ProcCmd := e_NODE_Get
      io.Sta_out.Dir.Pending := true.B
      io.Sta_out.HomeUniMsg.Cmd := e_UNI_Get
      io.Sta_out.HomeUniMsg.Proc := io.Sta_out.Dir.HeadPtr
      io.Sta_out.HomeUniMsg.HomeProc := io.Sta_out.Dir.HomeHeadPtr
    }
  }
  
}

class rule_PI_Local_Get_Put(NODE_NUM:Int,DATA_NUM:Int) extends rule(NODE_NUM,DATA_NUM){


  when(io.en_r){
  
    when(((io.Sta_in.HomeProc.ProcCmd === e_NODE_None) && ((io.Sta_in.HomeProc.CacheState === e_CACHE_I) && (!(io.Sta_in.Dir.Pending) && !(io.Sta_in.Dir.Dirty))))){
      io.Sta_out.Dir.Local := true.B
      io.Sta_out.HomeProc.ProcCmd := e_NODE_None
      when(io.Sta_in.HomeProc.InvMarked){
        io.Sta_out.HomeProc.InvMarked := false.B
        io.Sta_out.HomeProc.CacheState := e_CACHE_I
      }
      .otherwise{
        io.Sta_out.HomeProc.CacheState := e_CACHE_S
        io.Sta_out.HomeProc.CacheData := io.Sta_out.MemData
      }
    }
  }
  
}

class rule_PI_Remote_GetX(src:Int,NODE_NUM:Int,DATA_NUM:Int) extends rule(NODE_NUM,DATA_NUM){


  when(io.en_r){
  
    when(((io.Sta_in.Proc(src).ProcCmd === e_NODE_None) && (io.Sta_in.Proc(src).CacheState === e_CACHE_I))){
      io.Sta_out.Proc(src).ProcCmd := e_NODE_GetX
      io.Sta_out.UniMsg(src).Cmd := e_UNI_GetX
      io.Sta_out.UniMsg(src).HomeProc := true.B
    }
  }
  
}

class rule_PI_Local_GetX_GetX(NODE_NUM:Int,DATA_NUM:Int) extends rule(NODE_NUM,DATA_NUM){


  when(io.en_r){
  
    when(((io.Sta_in.HomeProc.ProcCmd === e_NODE_None) && (((io.Sta_in.HomeProc.CacheState === e_CACHE_I) || (io.Sta_in.HomeProc.CacheState === e_CACHE_S)) && (!(io.Sta_in.Dir.Pending) && io.Sta_in.Dir.Dirty)))){
      io.Sta_out.HomeProc.ProcCmd := e_NODE_GetX
      io.Sta_out.Dir.Pending := true.B
      io.Sta_out.HomeUniMsg.Cmd := e_UNI_GetX
      io.Sta_out.HomeUniMsg.Proc := io.Sta_out.Dir.HeadPtr
      io.Sta_out.HomeUniMsg.HomeProc := io.Sta_out.Dir.HomeHeadPtr
    }
  }
  
}

class rule_PI_Local_GetX_PutX_HeadVld(NODE_NUM:Int,DATA_NUM:Int) extends rule(NODE_NUM,DATA_NUM){


  when(io.en_r){
  
    when(((io.Sta_in.HomeProc.ProcCmd === e_NODE_None) && (((io.Sta_in.HomeProc.CacheState === e_CACHE_I) || (io.Sta_in.HomeProc.CacheState === e_CACHE_S)) && (!(io.Sta_in.Dir.Pending) && (!(io.Sta_in.Dir.Dirty) && io.Sta_in.Dir.HeadVld))))){
      io.Sta_out.Dir.Local := true.B
      io.Sta_out.Dir.Dirty := true.B
      io.Sta_out.Dir.Pending := true.B
      io.Sta_out.Dir.HeadVld := false.B
      io.Sta_out.Dir.ShrVld := false.B
      for( p <- 0 until NODE_NUM){
        io.Sta_out.Dir.ShrSet(p) := false.B
        when(((io.Sta_in.Dir.ShrVld && io.Sta_in.Dir.ShrSet(p)) || ((io.Sta_in.Dir.HeadPtr === p.U) && !(io.Sta_in.Dir.HomeHeadPtr)))){
          io.Sta_out.Dir.InvSet(p) := true.B
          io.Sta_out.InvMsg(p).Cmd := e_INV_Inv
        }
        .otherwise{
          io.Sta_out.Dir.InvSet(p) := false.B
          io.Sta_out.InvMsg(p).Cmd := e_INV_None
        }
      }
      io.Sta_out.Dir.HomeShrSet := false.B
      io.Sta_out.Dir.HomeInvSet := false.B
      io.Sta_out.HomeInvMsg.Cmd := e_INV_None
      io.Sta_out.HomeProc.ProcCmd := e_NODE_None
      io.Sta_out.HomeProc.InvMarked := false.B
      io.Sta_out.HomeProc.CacheState := e_CACHE_E
      io.Sta_out.HomeProc.CacheData := io.Sta_out.MemData
    }
  }
  
}

class rule_PI_Local_GetX_PutX(NODE_NUM:Int,DATA_NUM:Int) extends rule(NODE_NUM,DATA_NUM){


  when(io.en_r){
  
    when(((io.Sta_in.HomeProc.ProcCmd === e_NODE_None) && (((io.Sta_in.HomeProc.CacheState === e_CACHE_I) || (io.Sta_in.HomeProc.CacheState === e_CACHE_S)) && (!(io.Sta_in.Dir.Pending) && (!(io.Sta_in.Dir.Dirty) && !(io.Sta_in.Dir.HeadVld)))))){
      io.Sta_out.Dir.Local := true.B
      io.Sta_out.Dir.Dirty := true.B
      io.Sta_out.HomeProc.ProcCmd := e_NODE_None
      io.Sta_out.HomeProc.InvMarked := false.B
      io.Sta_out.HomeProc.CacheState := e_CACHE_E
      io.Sta_out.HomeProc.CacheData := io.Sta_out.MemData
    }
  }
  
}

class rule_PI_Remote_PutX(dst:Int,NODE_NUM:Int,DATA_NUM:Int) extends rule(NODE_NUM,DATA_NUM){


  when(io.en_r){
  
    when(((io.Sta_in.Proc(dst).ProcCmd === e_NODE_None) && (io.Sta_in.Proc(dst).CacheState === e_CACHE_E))){
      io.Sta_out.Proc(dst).CacheState := e_CACHE_I
      io.Sta_out.WbMsg.Cmd := e_WB_Wb
      io.Sta_out.WbMsg.Proc := dst.U
      io.Sta_out.WbMsg.HomeProc := false.B
      io.Sta_out.WbMsg.Data := io.Sta_out.Proc(dst).CacheData
    }
  }
  
}

class rule_PI_Local_PutX(NODE_NUM:Int,DATA_NUM:Int) extends rule(NODE_NUM,DATA_NUM){


  when(io.en_r){
  
    when(((io.Sta_in.HomeProc.ProcCmd === e_NODE_None) && (io.Sta_in.HomeProc.CacheState === e_CACHE_E))){
      when(io.Sta_in.Dir.Pending){
        io.Sta_out.HomeProc.CacheState := e_CACHE_I
        io.Sta_out.Dir.Dirty := false.B
        io.Sta_out.MemData := io.Sta_out.HomeProc.CacheData
      }
      .otherwise{
        io.Sta_out.HomeProc.CacheState := e_CACHE_I
        io.Sta_out.Dir.Local := false.B
        io.Sta_out.Dir.Dirty := false.B
        io.Sta_out.MemData := io.Sta_out.HomeProc.CacheData
      }
    }
  }
  
}

class rule_PI_Remote_Replace(src:Int,NODE_NUM:Int,DATA_NUM:Int) extends rule(NODE_NUM,DATA_NUM){


  when(io.en_r){
  
    when(((io.Sta_in.Proc(src).ProcCmd === e_NODE_None) && (io.Sta_in.Proc(src).CacheState === e_CACHE_S))){
      io.Sta_out.Proc(src).CacheState := e_CACHE_I
      io.Sta_out.RpMsg(src).Cmd := e_RP_Replace
    }
  }
  
}

class rule_PI_Local_Replace(NODE_NUM:Int,DATA_NUM:Int) extends rule(NODE_NUM,DATA_NUM){


  when(io.en_r){
  
    when(((io.Sta_in.HomeProc.ProcCmd === e_NODE_None) && (io.Sta_in.HomeProc.CacheState === e_CACHE_S))){
      io.Sta_out.Dir.Local := false.B
      io.Sta_out.HomeProc.CacheState := e_CACHE_I
    }
  }
  
}

class rule_NI_Nak(dst:Int,NODE_NUM:Int,DATA_NUM:Int) extends rule(NODE_NUM,DATA_NUM){


  when(io.en_r){
  
    when((io.Sta_in.UniMsg(dst).Cmd === e_UNI_Nak)){
      io.Sta_out.UniMsg(dst).Cmd := e_UNI_None
      io.Sta_out.Proc(dst).ProcCmd := e_NODE_None
      io.Sta_out.Proc(dst).InvMarked := false.B
    }
  }
  
}

class rule_NI_Nak_Home(NODE_NUM:Int,DATA_NUM:Int) extends rule(NODE_NUM,DATA_NUM){


  when(io.en_r){
  
    when((io.Sta_in.HomeUniMsg.Cmd === e_UNI_Nak)){
      io.Sta_out.HomeUniMsg.Cmd := e_UNI_None
      io.Sta_out.HomeProc.ProcCmd := e_NODE_None
      io.Sta_out.HomeProc.InvMarked := false.B
    }
  }
  
}

class rule_NI_Nak_Clear(NODE_NUM:Int,DATA_NUM:Int) extends rule(NODE_NUM,DATA_NUM){


  when(io.en_r){
  
    when((io.Sta_in.NakcMsg.Cmd === e_NAKC_Nakc)){
      io.Sta_out.NakcMsg.Cmd := e_NAKC_None
      io.Sta_out.Dir.Pending := false.B
    }
  }
  
}

class rule_NI_Local_Get_Nak(src:Int,NODE_NUM:Int,DATA_NUM:Int) extends rule(NODE_NUM,DATA_NUM){


  when(io.en_r){
  
    when(((io.Sta_in.UniMsg(src).Cmd === e_UNI_Get) && (io.Sta_in.UniMsg(src).HomeProc && ((io.Sta_in.RpMsg(src).Cmd =/= e_RP_Replace) && (io.Sta_in.Dir.Pending || ((io.Sta_in.Dir.Dirty && (io.Sta_in.Dir.Local && (io.Sta_in.HomeProc.CacheState =/= e_CACHE_E))) || (io.Sta_in.Dir.Dirty && (!(io.Sta_in.Dir.Local) && ((io.Sta_in.Dir.HeadPtr === src.U) && !(io.Sta_in.Dir.HomeHeadPtr)))))))))){
      io.Sta_out.UniMsg(src).Cmd := e_UNI_Nak
    }
  }
  
}

class rule_NI_Local_Get_Get(src:Int,NODE_NUM:Int,DATA_NUM:Int) extends rule(NODE_NUM,DATA_NUM){


  when(io.en_r){
  
    when(((io.Sta_in.UniMsg(src).Cmd === e_UNI_Get) && (io.Sta_in.UniMsg(src).HomeProc && ((io.Sta_in.RpMsg(src).Cmd =/= e_RP_Replace) && (!(io.Sta_in.Dir.Pending) && (io.Sta_in.Dir.Dirty && (!(io.Sta_in.Dir.Local) && ((io.Sta_in.Dir.HeadPtr =/= src.U) || io.Sta_in.Dir.HomeHeadPtr)))))))){
      io.Sta_out.Dir.Pending := true.B
      io.Sta_out.UniMsg(src).Cmd := e_UNI_Get
      io.Sta_out.UniMsg(src).Proc := io.Sta_out.Dir.HeadPtr
      io.Sta_out.UniMsg(src).HomeProc := io.Sta_out.Dir.HomeHeadPtr
    }
  }
  
}

class rule_NI_Local_Get_Put_Head(src:Int,NODE_NUM:Int,DATA_NUM:Int) extends rule(NODE_NUM,DATA_NUM){


  when(io.en_r){
  
    when(((io.Sta_in.UniMsg(src).Cmd === e_UNI_Get) && (io.Sta_in.UniMsg(src).HomeProc && ((io.Sta_in.RpMsg(src).Cmd =/= e_RP_Replace) && (!(io.Sta_in.Dir.Pending) && (!(io.Sta_in.Dir.Dirty) && io.Sta_in.Dir.HeadVld)))))){
      io.Sta_out.Dir.ShrVld := true.B
      io.Sta_out.Dir.ShrSet(src) := true.B
      for( p <- 0 until NODE_NUM){
        when((p.U === src.U)){
          io.Sta_out.Dir.InvSet(p) := true.B
        }
        .otherwise{
          io.Sta_out.Dir.InvSet(p) := io.Sta_out.Dir.ShrSet(p)
        }
      }
      io.Sta_out.Dir.HomeInvSet := io.Sta_out.Dir.HomeShrSet
      io.Sta_out.UniMsg(src).Cmd := e_UNI_Put
      io.Sta_out.UniMsg(src).Data := io.Sta_out.MemData
    }
  }
  
}

class rule_NI_Local_Get_Put(src:Int,NODE_NUM:Int,DATA_NUM:Int) extends rule(NODE_NUM,DATA_NUM){


  when(io.en_r){
  
    when(((io.Sta_in.UniMsg(src).Cmd === e_UNI_Get) && (io.Sta_in.UniMsg(src).HomeProc && ((io.Sta_in.RpMsg(src).Cmd =/= e_RP_Replace) && (!(io.Sta_in.Dir.Pending) && (!(io.Sta_in.Dir.Dirty) && !(io.Sta_in.Dir.HeadVld))))))){
      io.Sta_out.Dir.HeadVld := true.B
      io.Sta_out.Dir.HeadPtr := src.U
      io.Sta_out.Dir.HomeHeadPtr := false.B
      io.Sta_out.UniMsg(src).Cmd := e_UNI_Put
      io.Sta_out.UniMsg(src).Data := io.Sta_out.MemData
    }
  }
  
}

class rule_NI_Local_Get_Put_Dirty(src:Int,NODE_NUM:Int,DATA_NUM:Int) extends rule(NODE_NUM,DATA_NUM){


  when(io.en_r){
  
    when(((io.Sta_in.UniMsg(src).Cmd === e_UNI_Get) && (io.Sta_in.UniMsg(src).HomeProc && ((io.Sta_in.RpMsg(src).Cmd =/= e_RP_Replace) && (!(io.Sta_in.Dir.Pending) && (io.Sta_in.Dir.Dirty && (io.Sta_in.Dir.Local && (io.Sta_in.HomeProc.CacheState === e_CACHE_E)))))))){
      io.Sta_out.Dir.Dirty := false.B
      io.Sta_out.Dir.HeadVld := true.B
      io.Sta_out.Dir.HeadPtr := src.U
      io.Sta_out.Dir.HomeHeadPtr := false.B
      io.Sta_out.MemData := io.Sta_out.HomeProc.CacheData
      io.Sta_out.HomeProc.CacheState := e_CACHE_S
      io.Sta_out.UniMsg(src).Cmd := e_UNI_Put
      io.Sta_out.UniMsg(src).Data := io.Sta_out.HomeProc.CacheData
    }
  }
  
}

class rule_NI_Remote_Get_Nak(src:Int,dst:Int,NODE_NUM:Int,DATA_NUM:Int) extends rule(NODE_NUM,DATA_NUM){


  when(io.en_r){
  
    when(((src.U =/= dst.U) && ((io.Sta_in.UniMsg(src).Cmd === e_UNI_Get) && ((io.Sta_in.UniMsg(src).Proc === dst.U) && (!(io.Sta_in.UniMsg(src).HomeProc) && (io.Sta_in.Proc(dst).CacheState =/= e_CACHE_E)))))){
      io.Sta_out.UniMsg(src).Cmd := e_UNI_Nak
      io.Sta_out.NakcMsg.Cmd := e_NAKC_Nakc
    }
  }
  
}

class rule_NI_Remote_Get_Nak_Home(dst:Int,NODE_NUM:Int,DATA_NUM:Int) extends rule(NODE_NUM,DATA_NUM){


  when(io.en_r){
  
    when(((io.Sta_in.HomeUniMsg.Cmd === e_UNI_Get) && ((io.Sta_in.HomeUniMsg.Proc === dst.U) && (!(io.Sta_in.HomeUniMsg.HomeProc) && (io.Sta_in.Proc(dst).CacheState =/= e_CACHE_E))))){
      io.Sta_out.HomeUniMsg.Cmd := e_UNI_Nak
      io.Sta_out.NakcMsg.Cmd := e_NAKC_Nakc
    }
  }
  
}

class rule_NI_Remote_Get_Put(src:Int,dst:Int,NODE_NUM:Int,DATA_NUM:Int) extends rule(NODE_NUM,DATA_NUM){


  when(io.en_r){
  
    when(((src.U =/= dst.U) && ((io.Sta_in.UniMsg(src).Cmd === e_UNI_Get) && ((io.Sta_in.UniMsg(src).Proc === dst.U) && (!(io.Sta_in.UniMsg(src).HomeProc) && (io.Sta_in.Proc(dst).CacheState === e_CACHE_E)))))){
      io.Sta_out.Proc(dst).CacheState := e_CACHE_S
      io.Sta_out.UniMsg(src).Cmd := e_UNI_Put
      io.Sta_out.UniMsg(src).Data := io.Sta_out.Proc(dst).CacheData
      io.Sta_out.ShWbMsg.Cmd := e_SHWB_ShWb
      io.Sta_out.ShWbMsg.Proc := src.U
      io.Sta_out.ShWbMsg.HomeProc := false.B
      io.Sta_out.ShWbMsg.Data := io.Sta_out.Proc(dst).CacheData
    }
  }
  
}

class rule_NI_Remote_Get_Put_Home(dst:Int,NODE_NUM:Int,DATA_NUM:Int) extends rule(NODE_NUM,DATA_NUM){


  when(io.en_r){
  
    when(((io.Sta_in.HomeUniMsg.Cmd === e_UNI_Get) && ((io.Sta_in.HomeUniMsg.Proc === dst.U) && (!(io.Sta_in.HomeUniMsg.HomeProc) && (io.Sta_in.Proc(dst).CacheState === e_CACHE_E))))){
      io.Sta_out.Proc(dst).CacheState := e_CACHE_S
      io.Sta_out.HomeUniMsg.Cmd := e_UNI_Put
      io.Sta_out.HomeUniMsg.Data := io.Sta_out.Proc(dst).CacheData
    }
  }
  
}

class rule_NI_Local_GetX_Nak(src:Int,NODE_NUM:Int,DATA_NUM:Int) extends rule(NODE_NUM,DATA_NUM){


  when(io.en_r){
  
    when(((io.Sta_in.UniMsg(src).Cmd === e_UNI_GetX) && (io.Sta_in.UniMsg(src).HomeProc && (io.Sta_in.Dir.Pending || ((io.Sta_in.Dir.Dirty && (io.Sta_in.Dir.Local && (io.Sta_in.HomeProc.CacheState =/= e_CACHE_E))) || (io.Sta_in.Dir.Dirty && (!(io.Sta_in.Dir.Local) && ((io.Sta_in.Dir.HeadPtr === src.U) && !(io.Sta_in.Dir.HomeHeadPtr))))))))){
      io.Sta_out.UniMsg(src).Cmd := e_UNI_Nak
    }
  }
  
}

class rule_NI_Local_GetX_GetX(src:Int,NODE_NUM:Int,DATA_NUM:Int) extends rule(NODE_NUM,DATA_NUM){


  when(io.en_r){
  
    when(((io.Sta_in.UniMsg(src).Cmd === e_UNI_GetX) && (io.Sta_in.UniMsg(src).HomeProc && (!(io.Sta_in.Dir.Pending) && (io.Sta_in.Dir.Dirty && (!(io.Sta_in.Dir.Local) && ((io.Sta_in.Dir.HeadPtr =/= src.U) || io.Sta_in.Dir.HomeHeadPtr))))))){
      io.Sta_out.Dir.Pending := true.B
      io.Sta_out.UniMsg(src).Cmd := e_UNI_GetX
      io.Sta_out.UniMsg(src).Proc := io.Sta_out.Dir.HeadPtr
      io.Sta_out.UniMsg(src).HomeProc := io.Sta_out.Dir.HomeHeadPtr
    }
  }
  
}

class rule_NI_Local_GetX_PutX_1(src:Int,NODE_NUM:Int,DATA_NUM:Int) extends rule(NODE_NUM,DATA_NUM){


  when(io.en_r){
  
    when(((io.Sta_in.UniMsg(src).Cmd === e_UNI_GetX) && (io.Sta_in.UniMsg(src).HomeProc && (!(io.Sta_in.Dir.Pending) && (!(io.Sta_in.Dir.Dirty) && (!(io.Sta_in.Dir.HeadVld) && (io.Sta_in.Dir.Local && (io.Sta_in.HomeProc.ProcCmd === e_NODE_Get)))))))){
      io.Sta_out.Dir.Local := false.B
      io.Sta_out.Dir.Dirty := true.B
      io.Sta_out.Dir.HeadVld := true.B
      io.Sta_out.Dir.HeadPtr := src.U
      io.Sta_out.Dir.HomeHeadPtr := false.B
      io.Sta_out.Dir.ShrVld := false.B
      for( p <- 0 until NODE_NUM){
        io.Sta_out.Dir.ShrSet(p) := false.B
        io.Sta_out.Dir.InvSet(p) := false.B
      }
      io.Sta_out.Dir.HomeShrSet := false.B
      io.Sta_out.Dir.HomeInvSet := false.B
      io.Sta_out.UniMsg(src).Cmd := e_UNI_PutX
      io.Sta_out.UniMsg(src).Data := io.Sta_out.MemData
      io.Sta_out.HomeProc.CacheState := e_CACHE_I
      io.Sta_out.HomeProc.InvMarked := true.B
    }
  }
  
}

class rule_NI_Local_GetX_PutX_2(src:Int,NODE_NUM:Int,DATA_NUM:Int) extends rule(NODE_NUM,DATA_NUM){


  when(io.en_r){
  
    when(((io.Sta_in.UniMsg(src).Cmd === e_UNI_GetX) && (io.Sta_in.UniMsg(src).HomeProc && (!(io.Sta_in.Dir.Pending) && (!(io.Sta_in.Dir.Dirty) && (!(io.Sta_in.Dir.HeadVld) && (io.Sta_in.Dir.Local && (io.Sta_in.HomeProc.ProcCmd =/= e_NODE_Get)))))))){
      io.Sta_out.Dir.Local := false.B
      io.Sta_out.Dir.Dirty := true.B
      io.Sta_out.Dir.HeadVld := true.B
      io.Sta_out.Dir.HeadPtr := src.U
      io.Sta_out.Dir.HomeHeadPtr := false.B
      io.Sta_out.Dir.ShrVld := false.B
      for( p <- 0 until NODE_NUM){
        io.Sta_out.Dir.ShrSet(p) := false.B
        io.Sta_out.Dir.InvSet(p) := false.B
      }
      io.Sta_out.Dir.HomeShrSet := false.B
      io.Sta_out.Dir.HomeInvSet := false.B
      io.Sta_out.UniMsg(src).Cmd := e_UNI_PutX
      io.Sta_out.UniMsg(src).Data := io.Sta_out.MemData
      io.Sta_out.HomeProc.CacheState := e_CACHE_I
    }
  }
  
}

class rule_NI_Local_GetX_PutX_3(src:Int,NODE_NUM:Int,DATA_NUM:Int) extends rule(NODE_NUM,DATA_NUM){


  when(io.en_r){
  
    when(((io.Sta_in.UniMsg(src).Cmd === e_UNI_GetX) && (io.Sta_in.UniMsg(src).HomeProc && (!(io.Sta_in.Dir.Pending) && (!(io.Sta_in.Dir.Dirty) && (!(io.Sta_in.Dir.HeadVld) && !(io.Sta_in.Dir.Local))))))){
      io.Sta_out.Dir.Local := false.B
      io.Sta_out.Dir.Dirty := true.B
      io.Sta_out.Dir.HeadVld := true.B
      io.Sta_out.Dir.HeadPtr := src.U
      io.Sta_out.Dir.HomeHeadPtr := false.B
      io.Sta_out.Dir.ShrVld := false.B
      for( p <- 0 until NODE_NUM){
        io.Sta_out.Dir.ShrSet(p) := false.B
        io.Sta_out.Dir.InvSet(p) := false.B
      }
      io.Sta_out.Dir.HomeShrSet := false.B
      io.Sta_out.Dir.HomeInvSet := false.B
      io.Sta_out.UniMsg(src).Cmd := e_UNI_PutX
      io.Sta_out.UniMsg(src).Data := io.Sta_out.MemData
      io.Sta_out.HomeProc.CacheState := e_CACHE_I
    }
  }
  
}

class rule_NI_Local_GetX_PutX_4(src:Int,NODE_NUM:Int,DATA_NUM:Int) extends rule(NODE_NUM,DATA_NUM){


  when(io.en_r){
    var forall_flag = true.B
    for(p <- 0 until NODE_NUM){
    when(!((!((p.U =/= src.U))||(!(io.Sta_in.Dir.ShrSet(p)))))){
      forall_flag = false.B
    }
    }
    when(((io.Sta_in.UniMsg(src).Cmd === e_UNI_GetX) && (io.Sta_in.UniMsg(src).HomeProc && (!(io.Sta_in.Dir.Pending) && (!(io.Sta_in.Dir.Dirty) && ((io.Sta_in.Dir.HeadPtr === src.U) && (!(io.Sta_in.Dir.HomeHeadPtr) && (!(io.Sta_in.Dir.HomeShrSet) && (forall_flag && (io.Sta_in.Dir.Local && (io.Sta_in.HomeProc.ProcCmd === e_NODE_Get))))))))))){
      io.Sta_out.Dir.Local := false.B
      io.Sta_out.Dir.Dirty := true.B
      io.Sta_out.Dir.HeadVld := true.B
      io.Sta_out.Dir.HeadPtr := src.U
      io.Sta_out.Dir.HomeHeadPtr := false.B
      io.Sta_out.Dir.ShrVld := false.B
      for( p <- 0 until NODE_NUM){
        io.Sta_out.Dir.ShrSet(p) := false.B
        io.Sta_out.Dir.InvSet(p) := false.B
      }
      io.Sta_out.Dir.HomeShrSet := false.B
      io.Sta_out.Dir.HomeInvSet := false.B
      io.Sta_out.UniMsg(src).Cmd := e_UNI_PutX
      io.Sta_out.UniMsg(src).Data := io.Sta_out.MemData
      io.Sta_out.HomeProc.CacheState := e_CACHE_I
      io.Sta_out.HomeProc.InvMarked := true.B
    }
  }
  
}

class rule_NI_Local_GetX_PutX_5(src:Int,NODE_NUM:Int,DATA_NUM:Int) extends rule(NODE_NUM,DATA_NUM){


  when(io.en_r){
    var forall_flag = true.B
    for(p <- 0 until NODE_NUM){
    when(!((!((p.U =/= src.U))||(!(io.Sta_in.Dir.ShrSet(p)))))){
      forall_flag = false.B
    }
    }
    when(((io.Sta_in.UniMsg(src).Cmd === e_UNI_GetX) && (io.Sta_in.UniMsg(src).HomeProc && (!(io.Sta_in.Dir.Pending) && (!(io.Sta_in.Dir.Dirty) && ((io.Sta_in.Dir.HeadPtr === src.U) && (!(io.Sta_in.Dir.HomeHeadPtr) && (!(io.Sta_in.Dir.HomeShrSet) && (forall_flag && (io.Sta_in.Dir.Local && (io.Sta_in.HomeProc.ProcCmd =/= e_NODE_Get))))))))))){
      io.Sta_out.Dir.Local := false.B
      io.Sta_out.Dir.Dirty := true.B
      io.Sta_out.Dir.HeadVld := true.B
      io.Sta_out.Dir.HeadPtr := src.U
      io.Sta_out.Dir.HomeHeadPtr := false.B
      io.Sta_out.Dir.ShrVld := false.B
      for( p <- 0 until NODE_NUM){
        io.Sta_out.Dir.ShrSet(p) := false.B
        io.Sta_out.Dir.InvSet(p) := false.B
      }
      io.Sta_out.Dir.HomeShrSet := false.B
      io.Sta_out.Dir.HomeInvSet := false.B
      io.Sta_out.UniMsg(src).Cmd := e_UNI_PutX
      io.Sta_out.UniMsg(src).Data := io.Sta_out.MemData
      io.Sta_out.HomeProc.CacheState := e_CACHE_I
    }
  }
  
}

class rule_NI_Local_GetX_PutX_6(src:Int,NODE_NUM:Int,DATA_NUM:Int) extends rule(NODE_NUM,DATA_NUM){


  when(io.en_r){
    var forall_flag = true.B
    for(p <- 0 until NODE_NUM){
    when(!((!((p.U =/= src.U))||(!(io.Sta_in.Dir.ShrSet(p)))))){
      forall_flag = false.B
    }
    }
    when(((io.Sta_in.UniMsg(src).Cmd === e_UNI_GetX) && (io.Sta_in.UniMsg(src).HomeProc && (!(io.Sta_in.Dir.Pending) && (!(io.Sta_in.Dir.Dirty) && ((io.Sta_in.Dir.HeadPtr === src.U) && (!(io.Sta_in.Dir.HomeHeadPtr) && (!(io.Sta_in.Dir.HomeShrSet) && (forall_flag && !(io.Sta_in.Dir.Local)))))))))){
      io.Sta_out.Dir.Local := false.B
      io.Sta_out.Dir.Dirty := true.B
      io.Sta_out.Dir.HeadVld := true.B
      io.Sta_out.Dir.HeadPtr := src.U
      io.Sta_out.Dir.HomeHeadPtr := false.B
      io.Sta_out.Dir.ShrVld := false.B
      for( p <- 0 until NODE_NUM){
        io.Sta_out.Dir.ShrSet(p) := false.B
        io.Sta_out.Dir.InvSet(p) := false.B
      }
      io.Sta_out.Dir.HomeShrSet := false.B
      io.Sta_out.Dir.HomeInvSet := false.B
      io.Sta_out.UniMsg(src).Cmd := e_UNI_PutX
      io.Sta_out.UniMsg(src).Data := io.Sta_out.MemData
      io.Sta_out.HomeProc.CacheState := e_CACHE_I
    }
  }
  
}

class rule_NI_Local_GetX_PutX_7(src:Int,NODE_NUM:Int,DATA_NUM:Int) extends rule(NODE_NUM,DATA_NUM){


  when(io.en_r){
  
    when(((io.Sta_in.UniMsg(src).Cmd === e_UNI_GetX) && (io.Sta_in.UniMsg(src).HomeProc && (!(io.Sta_in.Dir.Pending) && (!(io.Sta_in.Dir.Dirty) && (io.Sta_in.Dir.HeadVld && (((io.Sta_in.Dir.HeadPtr =/= src.U) || io.Sta_in.Dir.HomeHeadPtr) && (io.Sta_in.Dir.Local && (io.Sta_in.HomeProc.ProcCmd =/= e_NODE_Get))))))))){
      io.Sta_out.Dir.Pending := true.B
      io.Sta_out.Dir.Local := false.B
      io.Sta_out.Dir.Dirty := true.B
      io.Sta_out.Dir.HeadVld := true.B
      io.Sta_out.Dir.HeadPtr := src.U
      io.Sta_out.Dir.HomeHeadPtr := false.B
      io.Sta_out.Dir.ShrVld := false.B
      for( p <- 0 until NODE_NUM){
        io.Sta_out.Dir.ShrSet(p) := false.B
        when(((p.U =/= src.U) && ((io.Sta_in.Dir.ShrVld && io.Sta_in.Dir.ShrSet(p)) || (io.Sta_in.Dir.HeadVld && ((io.Sta_in.Dir.HeadPtr === p.U) && !(io.Sta_in.Dir.HomeHeadPtr)))))){
          io.Sta_out.Dir.InvSet(p) := true.B
          io.Sta_out.InvMsg(p).Cmd := e_INV_Inv
        }
        .otherwise{
          io.Sta_out.Dir.InvSet(p) := false.B
          io.Sta_out.InvMsg(p).Cmd := e_INV_None
        }
      }
      io.Sta_out.Dir.HomeShrSet := false.B
      io.Sta_out.Dir.HomeInvSet := false.B
      io.Sta_out.HomeInvMsg.Cmd := e_INV_None
      io.Sta_out.UniMsg(src).Cmd := e_UNI_PutX
      io.Sta_out.UniMsg(src).Data := io.Sta_out.MemData
      io.Sta_out.HomeProc.CacheState := e_CACHE_I
    }
  }
  
}

class rule_NI_Local_GetX_PutX_7_NODE_Get(src:Int,NODE_NUM:Int,DATA_NUM:Int) extends rule(NODE_NUM,DATA_NUM){


  when(io.en_r){
  
    when(((io.Sta_in.UniMsg(src).Cmd === e_UNI_GetX) && (io.Sta_in.UniMsg(src).HomeProc && (!(io.Sta_in.Dir.Pending) && (!(io.Sta_in.Dir.Dirty) && (io.Sta_in.Dir.HeadVld && (((io.Sta_in.Dir.HeadPtr =/= src.U) || io.Sta_in.Dir.HomeHeadPtr) && (io.Sta_in.Dir.Local && (io.Sta_in.HomeProc.ProcCmd === e_NODE_Get))))))))){
      io.Sta_out.Dir.Pending := true.B
      io.Sta_out.Dir.Local := false.B
      io.Sta_out.Dir.Dirty := true.B
      io.Sta_out.Dir.HeadVld := true.B
      io.Sta_out.Dir.HeadPtr := src.U
      io.Sta_out.Dir.HomeHeadPtr := false.B
      io.Sta_out.Dir.ShrVld := false.B
      for( p <- 0 until NODE_NUM){
        io.Sta_out.Dir.ShrSet(p) := false.B
        when(((p.U =/= src.U) && ((io.Sta_in.Dir.ShrVld && io.Sta_in.Dir.ShrSet(p)) || (io.Sta_in.Dir.HeadVld && ((io.Sta_in.Dir.HeadPtr === p.U) && !(io.Sta_in.Dir.HomeHeadPtr)))))){
          io.Sta_out.Dir.InvSet(p) := true.B
          io.Sta_out.InvMsg(p).Cmd := e_INV_Inv
        }
        .otherwise{
          io.Sta_out.Dir.InvSet(p) := false.B
          io.Sta_out.InvMsg(p).Cmd := e_INV_None
        }
      }
      io.Sta_out.Dir.HomeShrSet := false.B
      io.Sta_out.Dir.HomeInvSet := false.B
      io.Sta_out.HomeInvMsg.Cmd := e_INV_None
      io.Sta_out.UniMsg(src).Cmd := e_UNI_PutX
      io.Sta_out.UniMsg(src).Data := io.Sta_out.MemData
      io.Sta_out.HomeProc.CacheState := e_CACHE_I
      io.Sta_out.HomeProc.InvMarked := true.B
    }
  }
  
}

class rule_NI_Local_GetX_PutX_8_Home(src:Int,NODE_NUM:Int,DATA_NUM:Int) extends rule(NODE_NUM,DATA_NUM){


  when(io.en_r){
  
    when(((io.Sta_in.UniMsg(src).Cmd === e_UNI_GetX) && (io.Sta_in.UniMsg(src).HomeProc && (!(io.Sta_in.Dir.Pending) && (!(io.Sta_in.Dir.Dirty) && (io.Sta_in.Dir.HeadVld && ((io.Sta_in.Dir.HeadPtr === src.U) && (!(io.Sta_in.Dir.HomeHeadPtr) && (io.Sta_in.Dir.HomeShrSet && (io.Sta_in.Dir.Local && (io.Sta_in.HomeProc.ProcCmd =/= e_NODE_Get))))))))))){
      io.Sta_out.Dir.Pending := true.B
      io.Sta_out.Dir.Local := false.B
      io.Sta_out.Dir.Dirty := true.B
      io.Sta_out.Dir.HeadVld := true.B
      io.Sta_out.Dir.HeadPtr := src.U
      io.Sta_out.Dir.HomeHeadPtr := false.B
      io.Sta_out.Dir.ShrVld := false.B
      for( p <- 0 until NODE_NUM){
        io.Sta_out.Dir.ShrSet(p) := false.B
        when(((p.U =/= src.U) && ((io.Sta_in.Dir.ShrVld && io.Sta_in.Dir.ShrSet(p)) || (io.Sta_in.Dir.HeadVld && ((io.Sta_in.Dir.HeadPtr === p.U) && !(io.Sta_in.Dir.HomeHeadPtr)))))){
          io.Sta_out.Dir.InvSet(p) := true.B
          io.Sta_out.InvMsg(p).Cmd := e_INV_Inv
        }
        .otherwise{
          io.Sta_out.Dir.InvSet(p) := false.B
          io.Sta_out.InvMsg(p).Cmd := e_INV_None
        }
      }
      io.Sta_out.Dir.HomeShrSet := false.B
      io.Sta_out.Dir.HomeInvSet := false.B
      io.Sta_out.HomeInvMsg.Cmd := e_INV_None
      io.Sta_out.UniMsg(src).Cmd := e_UNI_PutX
      io.Sta_out.UniMsg(src).Data := io.Sta_out.MemData
      io.Sta_out.HomeProc.CacheState := e_CACHE_I
    }
  }
  
}

class rule_NI_Local_GetX_PutX_8_Home_NODE_Get(src:Int,NODE_NUM:Int,DATA_NUM:Int) extends rule(NODE_NUM,DATA_NUM){


  when(io.en_r){
  
    when(((io.Sta_in.UniMsg(src).Cmd === e_UNI_GetX) && (io.Sta_in.UniMsg(src).HomeProc && (!(io.Sta_in.Dir.Pending) && (!(io.Sta_in.Dir.Dirty) && (io.Sta_in.Dir.HeadVld && ((io.Sta_in.Dir.HeadPtr === src.U) && (!(io.Sta_in.Dir.HomeHeadPtr) && (io.Sta_in.Dir.HomeShrSet && (io.Sta_in.Dir.Local && (io.Sta_in.HomeProc.ProcCmd === e_NODE_Get))))))))))){
      io.Sta_out.Dir.Pending := true.B
      io.Sta_out.Dir.Local := false.B
      io.Sta_out.Dir.Dirty := true.B
      io.Sta_out.Dir.HeadVld := true.B
      io.Sta_out.Dir.HeadPtr := src.U
      io.Sta_out.Dir.HomeHeadPtr := false.B
      io.Sta_out.Dir.ShrVld := false.B
      for( p <- 0 until NODE_NUM){
        io.Sta_out.Dir.ShrSet(p) := false.B
        when(((p.U =/= src.U) && ((io.Sta_in.Dir.ShrVld && io.Sta_in.Dir.ShrSet(p)) || (io.Sta_in.Dir.HeadVld && ((io.Sta_in.Dir.HeadPtr === p.U) && !(io.Sta_in.Dir.HomeHeadPtr)))))){
          io.Sta_out.Dir.InvSet(p) := true.B
          io.Sta_out.InvMsg(p).Cmd := e_INV_Inv
        }
        .otherwise{
          io.Sta_out.Dir.InvSet(p) := false.B
          io.Sta_out.InvMsg(p).Cmd := e_INV_None
        }
      }
      io.Sta_out.Dir.HomeShrSet := false.B
      io.Sta_out.Dir.HomeInvSet := false.B
      io.Sta_out.HomeInvMsg.Cmd := e_INV_None
      io.Sta_out.UniMsg(src).Cmd := e_UNI_PutX
      io.Sta_out.UniMsg(src).Data := io.Sta_out.MemData
      io.Sta_out.HomeProc.CacheState := e_CACHE_I
      io.Sta_out.HomeProc.InvMarked := true.B
    }
  }
  
}

class rule_NI_Local_GetX_PutX_8(src:Int,pp:Int,NODE_NUM:Int,DATA_NUM:Int) extends rule(NODE_NUM,DATA_NUM){


  when(io.en_r){
  
    when(((io.Sta_in.UniMsg(src).Cmd === e_UNI_GetX) && (io.Sta_in.UniMsg(src).HomeProc && (!(io.Sta_in.Dir.Pending) && (!(io.Sta_in.Dir.Dirty) && (io.Sta_in.Dir.HeadVld && ((io.Sta_in.Dir.HeadPtr === src.U) && (!(io.Sta_in.Dir.HomeHeadPtr) && (io.Sta_in.Dir.ShrSet(pp) && (io.Sta_in.Dir.Local && (io.Sta_in.HomeProc.ProcCmd =/= e_NODE_Get))))))))))){
      io.Sta_out.Dir.Pending := true.B
      io.Sta_out.Dir.Local := false.B
      io.Sta_out.Dir.Dirty := true.B
      io.Sta_out.Dir.HeadVld := true.B
      io.Sta_out.Dir.HeadPtr := src.U
      io.Sta_out.Dir.HomeHeadPtr := false.B
      io.Sta_out.Dir.ShrVld := false.B
      for( p <- 0 until NODE_NUM){
        io.Sta_out.Dir.ShrSet(p) := false.B
        when(((p.U =/= src.U) && ((io.Sta_in.Dir.ShrVld && io.Sta_in.Dir.ShrSet(p)) || (io.Sta_in.Dir.HeadVld && ((io.Sta_in.Dir.HeadPtr === p.U) && !(io.Sta_in.Dir.HomeHeadPtr)))))){
          io.Sta_out.Dir.InvSet(p) := true.B
          io.Sta_out.InvMsg(p).Cmd := e_INV_Inv
        }
        .otherwise{
          io.Sta_out.Dir.InvSet(p) := false.B
          io.Sta_out.InvMsg(p).Cmd := e_INV_None
        }
      }
      io.Sta_out.Dir.HomeShrSet := false.B
      io.Sta_out.Dir.HomeInvSet := false.B
      io.Sta_out.HomeInvMsg.Cmd := e_INV_None
      io.Sta_out.UniMsg(src).Cmd := e_UNI_PutX
      io.Sta_out.UniMsg(src).Data := io.Sta_out.MemData
      io.Sta_out.HomeProc.CacheState := e_CACHE_I
    }
  }
  
}

class rule_NI_Local_GetX_PutX_8_NODE_Get(src:Int,pp:Int,NODE_NUM:Int,DATA_NUM:Int) extends rule(NODE_NUM,DATA_NUM){


  when(io.en_r){
  
    when(((io.Sta_in.UniMsg(src).Cmd === e_UNI_GetX) && (io.Sta_in.UniMsg(src).HomeProc && (!(io.Sta_in.Dir.Pending) && (!(io.Sta_in.Dir.Dirty) && (io.Sta_in.Dir.HeadVld && ((io.Sta_in.Dir.HeadPtr === src.U) && (!(io.Sta_in.Dir.HomeHeadPtr) && (io.Sta_in.Dir.ShrSet(pp) && (io.Sta_in.Dir.Local && (io.Sta_in.HomeProc.ProcCmd === e_NODE_Get))))))))))){
      io.Sta_out.Dir.Pending := true.B
      io.Sta_out.Dir.Local := false.B
      io.Sta_out.Dir.Dirty := true.B
      io.Sta_out.Dir.HeadVld := true.B
      io.Sta_out.Dir.HeadPtr := src.U
      io.Sta_out.Dir.HomeHeadPtr := false.B
      io.Sta_out.Dir.ShrVld := false.B
      for( p <- 0 until NODE_NUM){
        io.Sta_out.Dir.ShrSet(p) := false.B
        when(((p.U =/= src.U) && ((io.Sta_in.Dir.ShrVld && io.Sta_in.Dir.ShrSet(p)) || (io.Sta_in.Dir.HeadVld && ((io.Sta_in.Dir.HeadPtr === p.U) && !(io.Sta_in.Dir.HomeHeadPtr)))))){
          io.Sta_out.Dir.InvSet(p) := true.B
          io.Sta_out.InvMsg(p).Cmd := e_INV_Inv
        }
        .otherwise{
          io.Sta_out.Dir.InvSet(p) := false.B
          io.Sta_out.InvMsg(p).Cmd := e_INV_None
        }
      }
      io.Sta_out.Dir.HomeShrSet := false.B
      io.Sta_out.Dir.HomeInvSet := false.B
      io.Sta_out.HomeInvMsg.Cmd := e_INV_None
      io.Sta_out.UniMsg(src).Cmd := e_UNI_PutX
      io.Sta_out.UniMsg(src).Data := io.Sta_out.MemData
      io.Sta_out.HomeProc.CacheState := e_CACHE_I
      io.Sta_out.HomeProc.InvMarked := true.B
    }
  }
  
}

class rule_NI_Local_GetX_PutX_9(src:Int,NODE_NUM:Int,DATA_NUM:Int) extends rule(NODE_NUM,DATA_NUM){


  when(io.en_r){
  
    when(((io.Sta_in.UniMsg(src).Cmd === e_UNI_GetX) && (io.Sta_in.UniMsg(src).HomeProc && (!(io.Sta_in.Dir.Pending) && (!(io.Sta_in.Dir.Dirty) && (io.Sta_in.Dir.HeadVld && (((io.Sta_in.Dir.HeadPtr =/= src.U) || io.Sta_in.Dir.HomeHeadPtr) && !(io.Sta_in.Dir.Local)))))))){
      io.Sta_out.Dir.Pending := true.B
      io.Sta_out.Dir.Local := false.B
      io.Sta_out.Dir.Dirty := true.B
      io.Sta_out.Dir.HeadVld := true.B
      io.Sta_out.Dir.HeadPtr := src.U
      io.Sta_out.Dir.HomeHeadPtr := false.B
      io.Sta_out.Dir.ShrVld := false.B
      for( p <- 0 until NODE_NUM){
        io.Sta_out.Dir.ShrSet(p) := false.B
        when(((p.U =/= src.U) && ((io.Sta_in.Dir.ShrVld && io.Sta_in.Dir.ShrSet(p)) || (io.Sta_in.Dir.HeadVld && ((io.Sta_in.Dir.HeadPtr === p.U) && !(io.Sta_in.Dir.HomeHeadPtr)))))){
          io.Sta_out.Dir.InvSet(p) := true.B
          io.Sta_out.InvMsg(p).Cmd := e_INV_Inv
        }
        .otherwise{
          io.Sta_out.Dir.InvSet(p) := false.B
          io.Sta_out.InvMsg(p).Cmd := e_INV_None
        }
      }
      io.Sta_out.Dir.HomeShrSet := false.B
      io.Sta_out.Dir.HomeInvSet := false.B
      io.Sta_out.HomeInvMsg.Cmd := e_INV_None
      io.Sta_out.UniMsg(src).Cmd := e_UNI_PutX
      io.Sta_out.UniMsg(src).Data := io.Sta_out.MemData
    }
  }
  
}

class rule_NI_Local_GetX_PutX_10_Home(src:Int,NODE_NUM:Int,DATA_NUM:Int) extends rule(NODE_NUM,DATA_NUM){


  when(io.en_r){
  
    when(((io.Sta_in.UniMsg(src).Cmd === e_UNI_GetX) && (io.Sta_in.UniMsg(src).HomeProc && (!(io.Sta_in.Dir.Pending) && (!(io.Sta_in.Dir.Dirty) && (io.Sta_in.Dir.HeadVld && ((io.Sta_in.Dir.HeadPtr === src.U) && (!(io.Sta_in.Dir.HomeHeadPtr) && (io.Sta_in.Dir.HomeShrSet && !(io.Sta_in.Dir.Local)))))))))){
      io.Sta_out.Dir.Pending := true.B
      io.Sta_out.Dir.Local := false.B
      io.Sta_out.Dir.Dirty := true.B
      io.Sta_out.Dir.HeadVld := true.B
      io.Sta_out.Dir.HeadPtr := src.U
      io.Sta_out.Dir.HomeHeadPtr := false.B
      io.Sta_out.Dir.ShrVld := false.B
      for( p <- 0 until NODE_NUM){
        io.Sta_out.Dir.ShrSet(p) := false.B
        when(((p.U =/= src.U) && ((io.Sta_in.Dir.ShrVld && io.Sta_in.Dir.ShrSet(p)) || (io.Sta_in.Dir.HeadVld && ((io.Sta_in.Dir.HeadPtr === p.U) && !(io.Sta_in.Dir.HomeHeadPtr)))))){
          io.Sta_out.Dir.InvSet(p) := true.B
          io.Sta_out.InvMsg(p).Cmd := e_INV_Inv
        }
        .otherwise{
          io.Sta_out.Dir.InvSet(p) := false.B
          io.Sta_out.InvMsg(p).Cmd := e_INV_None
        }
      }
      io.Sta_out.Dir.HomeShrSet := false.B
      io.Sta_out.Dir.HomeInvSet := false.B
      io.Sta_out.HomeInvMsg.Cmd := e_INV_None
      io.Sta_out.UniMsg(src).Cmd := e_UNI_PutX
      io.Sta_out.UniMsg(src).Data := io.Sta_out.MemData
    }
  }
  
}

class rule_NI_Local_GetX_PutX_10(src:Int,pp:Int,NODE_NUM:Int,DATA_NUM:Int) extends rule(NODE_NUM,DATA_NUM){


  when(io.en_r){
  
    when(((io.Sta_in.UniMsg(src).Cmd === e_UNI_GetX) && (io.Sta_in.UniMsg(src).HomeProc && (!(io.Sta_in.Dir.Pending) && (!(io.Sta_in.Dir.Dirty) && (io.Sta_in.Dir.HeadVld && ((io.Sta_in.Dir.HeadPtr === src.U) && (!(io.Sta_in.Dir.HomeHeadPtr) && (io.Sta_in.Dir.ShrSet(pp) && !(io.Sta_in.Dir.Local)))))))))){
      io.Sta_out.Dir.Pending := true.B
      io.Sta_out.Dir.Local := false.B
      io.Sta_out.Dir.Dirty := true.B
      io.Sta_out.Dir.HeadVld := true.B
      io.Sta_out.Dir.HeadPtr := src.U
      io.Sta_out.Dir.HomeHeadPtr := false.B
      io.Sta_out.Dir.ShrVld := false.B
      for( p <- 0 until NODE_NUM){
        io.Sta_out.Dir.ShrSet(p) := false.B
        when(((p.U =/= src.U) && ((io.Sta_in.Dir.ShrVld && io.Sta_in.Dir.ShrSet(p)) || (io.Sta_in.Dir.HeadVld && ((io.Sta_in.Dir.HeadPtr === p.U) && !(io.Sta_in.Dir.HomeHeadPtr)))))){
          io.Sta_out.Dir.InvSet(p) := true.B
          io.Sta_out.InvMsg(p).Cmd := e_INV_Inv
        }
        .otherwise{
          io.Sta_out.Dir.InvSet(p) := false.B
          io.Sta_out.InvMsg(p).Cmd := e_INV_None
        }
      }
      io.Sta_out.Dir.HomeShrSet := false.B
      io.Sta_out.Dir.HomeInvSet := false.B
      io.Sta_out.HomeInvMsg.Cmd := e_INV_None
      io.Sta_out.UniMsg(src).Cmd := e_UNI_PutX
      io.Sta_out.UniMsg(src).Data := io.Sta_out.MemData
    }
  }
  
}

class rule_NI_Local_GetX_PutX_11(src:Int,NODE_NUM:Int,DATA_NUM:Int) extends rule(NODE_NUM,DATA_NUM){


  when(io.en_r){
  
    when(((io.Sta_in.UniMsg(src).Cmd === e_UNI_GetX) && (io.Sta_in.UniMsg(src).HomeProc && (!(io.Sta_in.Dir.Pending) && (io.Sta_in.Dir.Dirty && (io.Sta_in.Dir.Local && (io.Sta_in.HomeProc.CacheState === e_CACHE_E))))))){
      io.Sta_out.Dir.Local := false.B
      io.Sta_out.Dir.Dirty := true.B
      io.Sta_out.Dir.HeadVld := true.B
      io.Sta_out.Dir.HeadPtr := src.U
      io.Sta_out.Dir.HomeHeadPtr := false.B
      io.Sta_out.Dir.ShrVld := false.B
      for( p <- 0 until NODE_NUM){
        io.Sta_out.Dir.ShrSet(p) := false.B
        io.Sta_out.Dir.InvSet(p) := false.B
      }
      io.Sta_out.Dir.HomeShrSet := false.B
      io.Sta_out.Dir.HomeInvSet := false.B
      io.Sta_out.UniMsg(src).Cmd := e_UNI_PutX
      io.Sta_out.UniMsg(src).Data := io.Sta_out.HomeProc.CacheData
      io.Sta_out.HomeProc.CacheState := e_CACHE_I
    }
  }
  
}

class rule_NI_Remote_GetX_Nak(src:Int,dst:Int,NODE_NUM:Int,DATA_NUM:Int) extends rule(NODE_NUM,DATA_NUM){


  when(io.en_r){
  
    when(((src.U =/= dst.U) && ((io.Sta_in.UniMsg(src).Cmd === e_UNI_GetX) && ((io.Sta_in.UniMsg(src).Proc === dst.U) && (!(io.Sta_in.UniMsg(src).HomeProc) && (io.Sta_in.Proc(dst).CacheState =/= e_CACHE_E)))))){
      io.Sta_out.UniMsg(src).Cmd := e_UNI_Nak
      io.Sta_out.NakcMsg.Cmd := e_NAKC_Nakc
    }
  }
  
}

class rule_NI_Remote_GetX_Nak_Home(dst:Int,NODE_NUM:Int,DATA_NUM:Int) extends rule(NODE_NUM,DATA_NUM){


  when(io.en_r){
  
    when(((io.Sta_in.HomeUniMsg.Cmd === e_UNI_GetX) && ((io.Sta_in.HomeUniMsg.Proc === dst.U) && (!(io.Sta_in.HomeUniMsg.HomeProc) && (io.Sta_in.Proc(dst).CacheState =/= e_CACHE_E))))){
      io.Sta_out.HomeUniMsg.Cmd := e_UNI_Nak
      io.Sta_out.NakcMsg.Cmd := e_NAKC_Nakc
    }
  }
  
}

class rule_NI_Remote_GetX_PutX(src:Int,dst:Int,NODE_NUM:Int,DATA_NUM:Int) extends rule(NODE_NUM,DATA_NUM){


  when(io.en_r){
  
    when(((src.U =/= dst.U) && ((io.Sta_in.UniMsg(src).Cmd === e_UNI_GetX) && ((io.Sta_in.UniMsg(src).Proc === dst.U) && (!(io.Sta_in.UniMsg(src).HomeProc) && (io.Sta_in.Proc(dst).CacheState === e_CACHE_E)))))){
      io.Sta_out.Proc(dst).CacheState := e_CACHE_I
      io.Sta_out.UniMsg(src).Cmd := e_UNI_PutX
      io.Sta_out.UniMsg(src).Data := io.Sta_out.Proc(dst).CacheData
      io.Sta_out.ShWbMsg.Cmd := e_SHWB_FAck
      io.Sta_out.ShWbMsg.Proc := src.U
      io.Sta_out.ShWbMsg.HomeProc := false.B
    }
  }
  
}

class rule_NI_Remote_GetX_PutX_Home(dst:Int,NODE_NUM:Int,DATA_NUM:Int) extends rule(NODE_NUM,DATA_NUM){


  when(io.en_r){
  
    when(((io.Sta_in.HomeUniMsg.Cmd === e_UNI_GetX) && ((io.Sta_in.HomeUniMsg.Proc === dst.U) && (!(io.Sta_in.HomeUniMsg.HomeProc) && (io.Sta_in.Proc(dst).CacheState === e_CACHE_E))))){
      io.Sta_out.Proc(dst).CacheState := e_CACHE_I
      io.Sta_out.HomeUniMsg.Cmd := e_UNI_PutX
      io.Sta_out.HomeUniMsg.Data := io.Sta_out.Proc(dst).CacheData
    }
  }
  
}

class rule_NI_Local_Put(NODE_NUM:Int,DATA_NUM:Int) extends rule(NODE_NUM,DATA_NUM){


  when(io.en_r){
  
    when((io.Sta_in.HomeUniMsg.Cmd === e_UNI_Put)){
      io.Sta_out.HomeUniMsg.Cmd := e_UNI_None
      io.Sta_out.Dir.Pending := false.B
      io.Sta_out.Dir.Dirty := false.B
      io.Sta_out.Dir.Local := true.B
      io.Sta_out.MemData := io.Sta_out.HomeUniMsg.Data
      io.Sta_out.HomeProc.ProcCmd := e_NODE_None
      when(io.Sta_in.HomeProc.InvMarked){
        io.Sta_out.HomeProc.InvMarked := false.B
        io.Sta_out.HomeProc.CacheState := e_CACHE_I
      }
      .otherwise{
        io.Sta_out.HomeProc.CacheState := e_CACHE_S
        io.Sta_out.HomeProc.CacheData := io.Sta_out.HomeUniMsg.Data
      }
    }
  }
  
}

class rule_NI_Remote_Put(dst:Int,NODE_NUM:Int,DATA_NUM:Int) extends rule(NODE_NUM,DATA_NUM){


  when(io.en_r){
  
    when((io.Sta_in.UniMsg(dst).Cmd === e_UNI_Put)){
      io.Sta_out.UniMsg(dst).Cmd := e_UNI_None
      io.Sta_out.Proc(dst).ProcCmd := e_NODE_None
      when(io.Sta_in.Proc(dst).InvMarked){
        io.Sta_out.Proc(dst).InvMarked := false.B
        io.Sta_out.Proc(dst).CacheState := e_CACHE_I
      }
      .otherwise{
        io.Sta_out.Proc(dst).CacheState := e_CACHE_S
        io.Sta_out.Proc(dst).CacheData := io.Sta_out.UniMsg(dst).Data
      }
    }
  }
  
}

class rule_NI_Local_PutXAcksDone(NODE_NUM:Int,DATA_NUM:Int) extends rule(NODE_NUM,DATA_NUM){


  when(io.en_r){
  
    when((io.Sta_in.HomeUniMsg.Cmd === e_UNI_PutX)){
      io.Sta_out.HomeUniMsg.Cmd := e_UNI_None
      io.Sta_out.Dir.Pending := false.B
      io.Sta_out.Dir.Local := true.B
      io.Sta_out.Dir.HeadVld := false.B
      io.Sta_out.HomeProc.ProcCmd := e_NODE_None
      io.Sta_out.HomeProc.InvMarked := false.B
      io.Sta_out.HomeProc.CacheState := e_CACHE_E
      io.Sta_out.HomeProc.CacheData := io.Sta_out.HomeUniMsg.Data
    }
  }
  
}

class rule_NI_Remote_PutX(dst:Int,NODE_NUM:Int,DATA_NUM:Int) extends rule(NODE_NUM,DATA_NUM){


  when(io.en_r){
  
    when(((io.Sta_in.UniMsg(dst).Cmd === e_UNI_PutX) && (io.Sta_in.Proc(dst).ProcCmd === e_NODE_GetX))){
      io.Sta_out.UniMsg(dst).Cmd := e_UNI_None
      io.Sta_out.Proc(dst).ProcCmd := e_NODE_None
      io.Sta_out.Proc(dst).InvMarked := false.B
      io.Sta_out.Proc(dst).CacheState := e_CACHE_E
      io.Sta_out.Proc(dst).CacheData := io.Sta_out.UniMsg(dst).Data
    }
  }
  
}

class rule_NI_Inv(dst:Int,NODE_NUM:Int,DATA_NUM:Int) extends rule(NODE_NUM,DATA_NUM){


  when(io.en_r){
  
    when((io.Sta_in.InvMsg(dst).Cmd === e_INV_Inv)){
      io.Sta_out.InvMsg(dst).Cmd := e_INV_InvAck
      io.Sta_out.Proc(dst).CacheState := e_CACHE_I
      when((io.Sta_in.Proc(dst).ProcCmd === e_NODE_Get)){
        io.Sta_out.Proc(dst).InvMarked := true.B
      }
    }
  }
  
}

class rule_NI_InvAck_exists_Home(src:Int,NODE_NUM:Int,DATA_NUM:Int) extends rule(NODE_NUM,DATA_NUM){


  when(io.en_r){
  
    when(((io.Sta_in.InvMsg(src).Cmd === e_INV_InvAck) && (io.Sta_in.Dir.Pending && (io.Sta_in.Dir.InvSet(src) && io.Sta_in.Dir.HomeInvSet)))){
      io.Sta_out.InvMsg(src).Cmd := e_INV_None
      io.Sta_out.Dir.InvSet(src) := false.B
    }
  }
  
}

class rule_NI_InvAck_exists(src:Int,pp:Int,NODE_NUM:Int,DATA_NUM:Int) extends rule(NODE_NUM,DATA_NUM){


  when(io.en_r){
  
    when(((io.Sta_in.InvMsg(src).Cmd === e_INV_InvAck) && (io.Sta_in.Dir.Pending && (io.Sta_in.Dir.InvSet(src) && ((pp.U =/= src.U) && io.Sta_in.Dir.InvSet(pp)))))){
      io.Sta_out.InvMsg(src).Cmd := e_INV_None
      io.Sta_out.Dir.InvSet(src) := false.B
    }
  }
  
}

class rule_NI_InvAck_1(src:Int,NODE_NUM:Int,DATA_NUM:Int) extends rule(NODE_NUM,DATA_NUM){


  when(io.en_r){
    var forall_flag = true.B
    for(p <- 0 until NODE_NUM){
    when(!(((p.U === src.U) || !(io.Sta_in.Dir.InvSet(p))))){
      forall_flag = false.B
    }
    }
    when(((io.Sta_in.InvMsg(src).Cmd === e_INV_InvAck) && (io.Sta_in.Dir.Pending && (io.Sta_in.Dir.InvSet(src) && (io.Sta_in.Dir.Local && (!(io.Sta_in.Dir.Dirty) && (!(io.Sta_in.Dir.HomeInvSet) && forall_flag))))))){
      io.Sta_out.InvMsg(src).Cmd := e_INV_None
      io.Sta_out.Dir.InvSet(src) := false.B
      io.Sta_out.Dir.Pending := false.B
      io.Sta_out.Dir.Local := false.B
    }
  }
  
}

class rule_NI_InvAck_2(src:Int,NODE_NUM:Int,DATA_NUM:Int) extends rule(NODE_NUM,DATA_NUM){


  when(io.en_r){
    var forall_flag = true.B
    for(p <- 0 until NODE_NUM){
    when(!(((p.U === src.U) || !(io.Sta_in.Dir.InvSet(p))))){
      forall_flag = false.B
    }
    }
    when(((io.Sta_in.InvMsg(src).Cmd === e_INV_InvAck) && (io.Sta_in.Dir.Pending && (io.Sta_in.Dir.InvSet(src) && (!(io.Sta_in.Dir.Local) && (!(io.Sta_in.Dir.HomeInvSet) && forall_flag)))))){
      io.Sta_out.InvMsg(src).Cmd := e_INV_None
      io.Sta_out.Dir.InvSet(src) := false.B
      io.Sta_out.Dir.Pending := false.B
    }
  }
  
}

class rule_NI_InvAck_3(src:Int,NODE_NUM:Int,DATA_NUM:Int) extends rule(NODE_NUM,DATA_NUM){


  when(io.en_r){
    var forall_flag = true.B
    for(p <- 0 until NODE_NUM){
    when(!(((p.U === src.U) || !(io.Sta_in.Dir.InvSet(p))))){
      forall_flag = false.B
    }
    }
    when(((io.Sta_in.InvMsg(src).Cmd === e_INV_InvAck) && (io.Sta_in.Dir.Pending && (io.Sta_in.Dir.InvSet(src) && (io.Sta_in.Dir.Dirty && (!(io.Sta_in.Dir.HomeInvSet) && forall_flag)))))){
      io.Sta_out.InvMsg(src).Cmd := e_INV_None
      io.Sta_out.Dir.InvSet(src) := false.B
      io.Sta_out.Dir.Pending := false.B
    }
  }
  
}

class rule_NI_Wb(NODE_NUM:Int,DATA_NUM:Int) extends rule(NODE_NUM,DATA_NUM){


  when(io.en_r){
  
    when((io.Sta_in.WbMsg.Cmd === e_WB_Wb)){
      io.Sta_out.WbMsg.Cmd := e_WB_None
      io.Sta_out.Dir.Dirty := false.B
      io.Sta_out.Dir.HeadVld := false.B
      io.Sta_out.MemData := io.Sta_out.WbMsg.Data
    }
  }
  
}

class rule_NI_FAck(NODE_NUM:Int,DATA_NUM:Int) extends rule(NODE_NUM,DATA_NUM){


  when(io.en_r){
  
    when((io.Sta_in.ShWbMsg.Cmd === e_SHWB_FAck)){
      io.Sta_out.ShWbMsg.Cmd := e_SHWB_None
      io.Sta_out.Dir.Pending := false.B
      when(io.Sta_in.Dir.Dirty){
        io.Sta_out.Dir.HeadPtr := io.Sta_out.ShWbMsg.Proc
        io.Sta_out.Dir.HomeHeadPtr := io.Sta_out.ShWbMsg.HomeProc
      }
    }
  }
  
}

class rule_NI_ShWb(NODE_NUM:Int,DATA_NUM:Int) extends rule(NODE_NUM,DATA_NUM){


  when(io.en_r){
  
    when((io.Sta_in.ShWbMsg.Cmd === e_SHWB_ShWb)){
      io.Sta_out.ShWbMsg.Cmd := e_SHWB_None
      io.Sta_out.Dir.Pending := false.B
      io.Sta_out.Dir.Dirty := false.B
      io.Sta_out.Dir.ShrVld := true.B
      for( p <- 0 until NODE_NUM){
        when((((p.U === io.Sta_in.ShWbMsg.Proc) && !(io.Sta_in.ShWbMsg.HomeProc)) || io.Sta_in.Dir.ShrSet(p))){
          io.Sta_out.Dir.ShrSet(p) := true.B
          io.Sta_out.Dir.InvSet(p) := true.B
        }
        .otherwise{
          io.Sta_out.Dir.ShrSet(p) := false.B
          io.Sta_out.Dir.InvSet(p) := false.B
        }
      }
      when((io.Sta_in.ShWbMsg.HomeProc || io.Sta_in.Dir.HomeShrSet)){
        io.Sta_out.Dir.HomeShrSet := true.B
        io.Sta_out.Dir.HomeInvSet := true.B
      }
      .otherwise{
        io.Sta_out.Dir.HomeShrSet := false.B
        io.Sta_out.Dir.HomeInvSet := false.B
      }
      io.Sta_out.MemData := io.Sta_out.ShWbMsg.Data
    }
  }
  
}

class rule_NI_Replace(src:Int,NODE_NUM:Int,DATA_NUM:Int) extends rule(NODE_NUM,DATA_NUM){


  when(io.en_r){
  
    when((io.Sta_in.RpMsg(src).Cmd === e_RP_Replace)){
      io.Sta_out.RpMsg(src).Cmd := e_RP_None
      when(io.Sta_in.Dir.ShrVld){
        io.Sta_out.Dir.ShrSet(src) := false.B
        io.Sta_out.Dir.InvSet(src) := false.B
      }
    }
  }
  
}

class rule_NI_Replace_Home(NODE_NUM:Int,DATA_NUM:Int) extends rule(NODE_NUM,DATA_NUM){


  when(io.en_r){
  
    when((io.Sta_in.HomeRpMsg.Cmd === e_RP_Replace)){
      io.Sta_out.HomeRpMsg.Cmd := e_RP_None
      when(io.Sta_in.Dir.ShrVld){
        io.Sta_out.Dir.HomeShrSet := false.B
        io.Sta_out.Dir.HomeInvSet := false.B
      }
    }
  }
  
}

class flash(NODE_NUM:Int,DATA_NUM:Int) extends Module{

  val e_CACHE_I::e_CACHE_S::e_CACHE_E::Nil = Enum(3)
  val e_NODE_None::e_NODE_Get::e_NODE_GetX::Nil = Enum(3)
  val e_UNI_None::e_UNI_Get::e_UNI_GetX::e_UNI_Put::e_UNI_PutX::e_UNI_Nak::Nil = Enum(6)
  val e_INV_None::e_INV_Inv::e_INV_InvAck::Nil = Enum(3)
  val e_RP_None::e_RP_Replace::Nil = Enum(2)
  val e_WB_None::e_WB_Wb::Nil = Enum(2)
  val e_SHWB_None::e_SHWB_ShWb::e_SHWB_FAck::Nil = Enum(3)
  val e_NAKC_None::e_NAKC_Nakc::Nil = Enum(2)

  val io = IO(new Bundle {
    val en_a = Input(UInt(log2Ceil(1*NODE_NUM*DATA_NUM+1*DATA_NUM+37*NODE_NUM+8*NODE_NUM*NODE_NUM+15).W))
    val Sta_out = Output(new STATE(NODE_NUM,DATA_NUM))
  )
  
  val Sta_init = Wire(new STATE(NODE_NUM,DATA_NUM))
  val Sta_reg = RegInit(Sta_init)
  io.Sta_out:=Sta_reg
  
  Sta_init.MemData := 0.U
  Sta_init.Dir.Pending := false.B
  Sta_init.Dir.Local := false.B
  Sta_init.Dir.Dirty := false.B
  Sta_init.Dir.HeadVld := false.B
  Sta_init.Dir.HeadPtr := 0.U
  Sta_init.Dir.HomeHeadPtr := true.B
  Sta_init.Dir.ShrVld := false.B
  Sta_init.WbMsg.Cmd := e_WB_None
  Sta_init.WbMsg.Proc := 0.U
  Sta_init.WbMsg.HomeProc := true.B
  Sta_init.WbMsg.Data := 0.U
  Sta_init.ShWbMsg.Cmd := e_SHWB_None
  Sta_init.ShWbMsg.Proc := 0.U
  Sta_init.ShWbMsg.HomeProc := true.B
  Sta_init.ShWbMsg.Data := 0.U
  Sta_init.NakcMsg.Cmd := e_NAKC_None
  for( p <- 0 until NODE_NUM){
    Sta_init.Proc(p).ProcCmd := e_NODE_None
    Sta_init.Proc(p).InvMarked := false.B
    Sta_init.Proc(p).CacheState := e_CACHE_I
    Sta_init.Proc(p).CacheData := 0.U
    Sta_init.Dir.ShrSet(p) := false.B
    Sta_init.Dir.InvSet(p) := false.B
    Sta_init.UniMsg(p).Cmd := e_UNI_None
    Sta_init.UniMsg(p).Proc := 0.U
    Sta_init.UniMsg(p).HomeProc := true.B
    Sta_init.UniMsg(p).Data := 0.U
    Sta_init.InvMsg(p).Cmd := e_INV_None
    Sta_init.RpMsg(p).Cmd := e_RP_None
  }
  Sta_init.HomeProc.ProcCmd := e_NODE_None
  Sta_init.HomeProc.InvMarked := false.B
  Sta_init.HomeProc.CacheState := e_CACHE_I
  Sta_init.HomeProc.CacheData := 0.U
  Sta_init.Dir.HomeShrSet := false.B
  Sta_init.Dir.HomeInvSet := false.B
  Sta_init.HomeUniMsg.Cmd := e_UNI_None
  Sta_init.HomeUniMsg.Proc := 0.U
  Sta_init.HomeUniMsg.HomeProc := true.B
  Sta_init.HomeUniMsg.Data := 0.U
  Sta_init.HomeInvMsg.Cmd := e_INV_None
  Sta_init.HomeRpMsg.Cmd := e_RP_None
  Sta_init.CurrData := 0.U
  
  var rules = ArrayBuffer[rule]()
  var index = 0
  for(src <- 0 until NODE_NUM) {
    for(data <- 0 until DATA_NUM) {
      rules += Module(new rule_Store(src,data, NODE_NUM,DATA_NUM))
    }
  }
  
  for(data <- 0 until DATA_NUM) {
    rules += Module(new rule_Store_Home(data, NODE_NUM,DATA_NUM))
  }
  
  for(src <- 0 until NODE_NUM) {
    rules += Module(new rule_PI_Remote_Get(src, NODE_NUM,DATA_NUM))
  }
  
  rules += Module(new rule_PI_Local_Get_Get(NODE_NUM,DATA_NUM))
  rules += Module(new rule_PI_Local_Get_Put(NODE_NUM,DATA_NUM))
  for(src <- 0 until NODE_NUM) {
    rules += Module(new rule_PI_Remote_GetX(src, NODE_NUM,DATA_NUM))
  }
  
  rules += Module(new rule_PI_Local_GetX_GetX(NODE_NUM,DATA_NUM))
  rules += Module(new rule_PI_Local_GetX_PutX_HeadVld(NODE_NUM,DATA_NUM))
  rules += Module(new rule_PI_Local_GetX_PutX(NODE_NUM,DATA_NUM))
  for(dst <- 0 until NODE_NUM) {
    rules += Module(new rule_PI_Remote_PutX(dst, NODE_NUM,DATA_NUM))
  }
  
  rules += Module(new rule_PI_Local_PutX(NODE_NUM,DATA_NUM))
  for(src <- 0 until NODE_NUM) {
    rules += Module(new rule_PI_Remote_Replace(src, NODE_NUM,DATA_NUM))
  }
  
  rules += Module(new rule_PI_Local_Replace(NODE_NUM,DATA_NUM))
  for(dst <- 0 until NODE_NUM) {
    rules += Module(new rule_NI_Nak(dst, NODE_NUM,DATA_NUM))
  }
  
  rules += Module(new rule_NI_Nak_Home(NODE_NUM,DATA_NUM))
  rules += Module(new rule_NI_Nak_Clear(NODE_NUM,DATA_NUM))
  for(src <- 0 until NODE_NUM) {
    rules += Module(new rule_NI_Local_Get_Nak(src, NODE_NUM,DATA_NUM))
  }
  
  for(src <- 0 until NODE_NUM) {
    rules += Module(new rule_NI_Local_Get_Get(src, NODE_NUM,DATA_NUM))
  }
  
  for(src <- 0 until NODE_NUM) {
    rules += Module(new rule_NI_Local_Get_Put_Head(src, NODE_NUM,DATA_NUM))
  }
  
  for(src <- 0 until NODE_NUM) {
    rules += Module(new rule_NI_Local_Get_Put(src, NODE_NUM,DATA_NUM))
  }
  
  for(src <- 0 until NODE_NUM) {
    rules += Module(new rule_NI_Local_Get_Put_Dirty(src, NODE_NUM,DATA_NUM))
  }
  
  for(src <- 0 until NODE_NUM) {
    for(dst <- 0 until NODE_NUM) {
      rules += Module(new rule_NI_Remote_Get_Nak(src,dst, NODE_NUM,DATA_NUM))
    }
  }
  
  for(dst <- 0 until NODE_NUM) {
    rules += Module(new rule_NI_Remote_Get_Nak_Home(dst, NODE_NUM,DATA_NUM))
  }
  
  for(src <- 0 until NODE_NUM) {
    for(dst <- 0 until NODE_NUM) {
      rules += Module(new rule_NI_Remote_Get_Put(src,dst, NODE_NUM,DATA_NUM))
    }
  }
  
  for(dst <- 0 until NODE_NUM) {
    rules += Module(new rule_NI_Remote_Get_Put_Home(dst, NODE_NUM,DATA_NUM))
  }
  
  for(src <- 0 until NODE_NUM) {
    rules += Module(new rule_NI_Local_GetX_Nak(src, NODE_NUM,DATA_NUM))
  }
  
  for(src <- 0 until NODE_NUM) {
    rules += Module(new rule_NI_Local_GetX_GetX(src, NODE_NUM,DATA_NUM))
  }
  
  for(src <- 0 until NODE_NUM) {
    rules += Module(new rule_NI_Local_GetX_PutX_1(src, NODE_NUM,DATA_NUM))
  }
  
  for(src <- 0 until NODE_NUM) {
    rules += Module(new rule_NI_Local_GetX_PutX_2(src, NODE_NUM,DATA_NUM))
  }
  
  for(src <- 0 until NODE_NUM) {
    rules += Module(new rule_NI_Local_GetX_PutX_3(src, NODE_NUM,DATA_NUM))
  }
  
  for(src <- 0 until NODE_NUM) {
    rules += Module(new rule_NI_Local_GetX_PutX_4(src, NODE_NUM,DATA_NUM))
  }
  
  for(src <- 0 until NODE_NUM) {
    rules += Module(new rule_NI_Local_GetX_PutX_5(src, NODE_NUM,DATA_NUM))
  }
  
  for(src <- 0 until NODE_NUM) {
    rules += Module(new rule_NI_Local_GetX_PutX_6(src, NODE_NUM,DATA_NUM))
  }
  
  for(src <- 0 until NODE_NUM) {
    rules += Module(new rule_NI_Local_GetX_PutX_7(src, NODE_NUM,DATA_NUM))
  }
  
  for(src <- 0 until NODE_NUM) {
    rules += Module(new rule_NI_Local_GetX_PutX_7_NODE_Get(src, NODE_NUM,DATA_NUM))
  }
  
  for(src <- 0 until NODE_NUM) {
    rules += Module(new rule_NI_Local_GetX_PutX_8_Home(src, NODE_NUM,DATA_NUM))
  }
  
  for(src <- 0 until NODE_NUM) {
    rules += Module(new rule_NI_Local_GetX_PutX_8_Home_NODE_Get(src, NODE_NUM,DATA_NUM))
  }
  
  for(src <- 0 until NODE_NUM) {
    for(pp <- 0 until NODE_NUM) {
      rules += Module(new rule_NI_Local_GetX_PutX_8(src,pp, NODE_NUM,DATA_NUM))
    }
  }
  
  for(src <- 0 until NODE_NUM) {
    for(pp <- 0 until NODE_NUM) {
      rules += Module(new rule_NI_Local_GetX_PutX_8_NODE_Get(src,pp, NODE_NUM,DATA_NUM))
    }
  }
  
  for(src <- 0 until NODE_NUM) {
    rules += Module(new rule_NI_Local_GetX_PutX_9(src, NODE_NUM,DATA_NUM))
  }
  
  for(src <- 0 until NODE_NUM) {
    rules += Module(new rule_NI_Local_GetX_PutX_10_Home(src, NODE_NUM,DATA_NUM))
  }
  
  for(src <- 0 until NODE_NUM) {
    for(pp <- 0 until NODE_NUM) {
      rules += Module(new rule_NI_Local_GetX_PutX_10(src,pp, NODE_NUM,DATA_NUM))
    }
  }
  
  for(src <- 0 until NODE_NUM) {
    rules += Module(new rule_NI_Local_GetX_PutX_11(src, NODE_NUM,DATA_NUM))
  }
  
  for(src <- 0 until NODE_NUM) {
    for(dst <- 0 until NODE_NUM) {
      rules += Module(new rule_NI_Remote_GetX_Nak(src,dst, NODE_NUM,DATA_NUM))
    }
  }
  
  for(dst <- 0 until NODE_NUM) {
    rules += Module(new rule_NI_Remote_GetX_Nak_Home(dst, NODE_NUM,DATA_NUM))
  }
  
  for(src <- 0 until NODE_NUM) {
    for(dst <- 0 until NODE_NUM) {
      rules += Module(new rule_NI_Remote_GetX_PutX(src,dst, NODE_NUM,DATA_NUM))
    }
  }
  
  for(dst <- 0 until NODE_NUM) {
    rules += Module(new rule_NI_Remote_GetX_PutX_Home(dst, NODE_NUM,DATA_NUM))
  }
  
  rules += Module(new rule_NI_Local_Put(NODE_NUM,DATA_NUM))
  for(dst <- 0 until NODE_NUM) {
    rules += Module(new rule_NI_Remote_Put(dst, NODE_NUM,DATA_NUM))
  }
  
  rules += Module(new rule_NI_Local_PutXAcksDone(NODE_NUM,DATA_NUM))
  for(dst <- 0 until NODE_NUM) {
    rules += Module(new rule_NI_Remote_PutX(dst, NODE_NUM,DATA_NUM))
  }
  
  for(dst <- 0 until NODE_NUM) {
    rules += Module(new rule_NI_Inv(dst, NODE_NUM,DATA_NUM))
  }
  
  for(src <- 0 until NODE_NUM) {
    rules += Module(new rule_NI_InvAck_exists_Home(src, NODE_NUM,DATA_NUM))
  }
  
  for(src <- 0 until NODE_NUM) {
    for(pp <- 0 until NODE_NUM) {
      rules += Module(new rule_NI_InvAck_exists(src,pp, NODE_NUM,DATA_NUM))
    }
  }
  
  for(src <- 0 until NODE_NUM) {
    rules += Module(new rule_NI_InvAck_1(src, NODE_NUM,DATA_NUM))
  }
  
  for(src <- 0 until NODE_NUM) {
    rules += Module(new rule_NI_InvAck_2(src, NODE_NUM,DATA_NUM))
  }
  
  for(src <- 0 until NODE_NUM) {
    rules += Module(new rule_NI_InvAck_3(src, NODE_NUM,DATA_NUM))
  }
  
  rules += Module(new rule_NI_Wb(NODE_NUM,DATA_NUM))
  rules += Module(new rule_NI_FAck(NODE_NUM,DATA_NUM))
  rules += Module(new rule_NI_ShWb(NODE_NUM,DATA_NUM))
  for(src <- 0 until NODE_NUM) {
    rules += Module(new rule_NI_Replace(src, NODE_NUM,DATA_NUM))
  }
  
  rules += Module(new rule_NI_Replace_Home(NODE_NUM,DATA_NUM))
  for(i <- 0 until 1*NODE_NUM*DATA_NUM+1*DATA_NUM+37*NODE_NUM+8*NODE_NUM*NODE_NUM+15) {
    rules(i).io.Sta_in := Sta_reg
    rules(i).io.en_r:=(io.en_a=== index.U)
    when(io.en_a=== index.U){
      Sta_reg := rules(i).io.Sta_out 
    }
    index = index +1 
  }
  
}

