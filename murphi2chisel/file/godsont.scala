import chisel3._
import chisel3.util._
import scala.collection.mutable.ArrayBuffer
class CACHE(val NUM_NODE:Int,val NUM_CACHE:Int,val NUM_ADDR:Int,val NUM_DATA:Int,val NUM_LOCK:Int) extends Bundle {
  val state = UInt(2.W)
  val addr = UInt(log2Ceil(NUM_ADDR).W)
  val data = UInt(log2Ceil(NUM_DATA).W)
}

class MEMORY(val NUM_NODE:Int,val NUM_CACHE:Int,val NUM_ADDR:Int,val NUM_DATA:Int,val NUM_LOCK:Int) extends Bundle {
  val data = UInt(log2Ceil(NUM_DATA).W)
}

class LOCK(val NUM_NODE:Int,val NUM_CACHE:Int,val NUM_ADDR:Int,val NUM_DATA:Int,val NUM_LOCK:Int) extends Bundle {
  val owner = UInt(log2Ceil(NUM_NODE).W)
  val beUsed = Bool()
  val inProtection = Vec(NUM_ADDR, Bool())
}

class NODE(val NUM_NODE:Int,val NUM_CACHE:Int,val NUM_ADDR:Int,val NUM_DATA:Int,val NUM_LOCK:Int) extends Bundle {
  val cache = Vec(NUM_CACHE, new CACHE(NUM_NODE,NUM_CACHE,NUM_ADDR,NUM_DATA,NUM_LOCK))
  val hasLock = Bool()
  val firstRead = Vec(NUM_ADDR, Bool())
}

class rule(NUM_NODE:Int,NUM_CACHE:Int,NUM_ADDR:Int,NUM_DATA:Int,NUM_LOCK:Int) extends Module{

  val e_INVALID::e_DIRTY::e_VALID::Nil = Enum(3)
  val e_NON::e_REQUIRE::e_REQREPALL::e_RANDOM::e_RANDINV::e_DESIGNATED::e_TOREP::e_DONE::e_REPALLDONE::Nil = Enum(9)
  val e_NONE::e_NLNCR::e_NLNCW::e_LNCFR::e_LCFR::e_LNCNFR::Nil = Enum(6)

  val io = IO(new Bundle {
    val en_r = Input(Bool())
    val memory_in = Input(Vec(NUM_ADDR,new MEMORY(NUM_NODE,NUM_CACHE,NUM_ADDR,NUM_DATA,NUM_LOCK)))
    val memory_out = Output(Vec(NUM_ADDR,new MEMORY(NUM_NODE,NUM_CACHE,NUM_ADDR,NUM_DATA,NUM_LOCK)))
    val lock_in = Input(Vec(NUM_LOCK,new LOCK(NUM_NODE,NUM_CACHE,NUM_ADDR,NUM_DATA,NUM_LOCK)))
    val lock_out = Output(Vec(NUM_LOCK,new LOCK(NUM_NODE,NUM_CACHE,NUM_ADDR,NUM_DATA,NUM_LOCK)))
    val node_in = Input(Vec(NUM_NODE,new NODE(NUM_NODE,NUM_CACHE,NUM_ADDR,NUM_DATA,NUM_LOCK)))
    val node_out = Output(Vec(NUM_NODE,new NODE(NUM_NODE,NUM_CACHE,NUM_ADDR,NUM_DATA,NUM_LOCK)))
    val curNode_in = Input(UInt(log2Ceil(NUM_NODE).W))
    val curNode_out = Output(UInt(log2Ceil(NUM_NODE).W))
    val curCache_in = Input(UInt(log2Ceil(NUM_CACHE).W))
    val curCache_out = Output(UInt(log2Ceil(NUM_CACHE).W))
    val curMemory_in = Input(UInt(log2Ceil(NUM_ADDR).W))
    val curMemory_out = Output(UInt(log2Ceil(NUM_ADDR).W))
    val curData_in = Input(UInt(log2Ceil(NUM_DATA).W))
    val curData_out = Output(UInt(log2Ceil(NUM_DATA).W))
    val curLock_in = Input(UInt(log2Ceil(NUM_LOCK).W))
    val curLock_out = Output(UInt(log2Ceil(NUM_LOCK).W))
    val replace_in = Input(UInt(4.W))
    val replace_out = Output(UInt(4.W))
    val repRule_in = Input(UInt(3.W))
    val repRule_out = Output(UInt(3.W))
  )
  
  io.memory_out:=io.memory_in
  io.lock_out:=io.lock_in
  io.node_out:=io.node_in
  io.curNode_out:=io.curNode_in
  io.curCache_out:=io.curCache_in
  io.curMemory_out:=io.curMemory_in
  io.curData_out:=io.curData_in
  io.curLock_out:=io.curLock_in
  io.replace_out:=io.replace_in
  io.repRule_out:=io.repRule_in
  
}

class rule_RI(i:Int,NUM_NODE:Int,NUM_CACHE:Int,NUM_ADDR:Int,NUM_DATA:Int,NUM_LOCK:Int) extends rule(NUM_NODE,NUM_CACHE,NUM_ADDR,NUM_DATA,NUM_LOCK){


  when(io.en_r){
  
    when(((io.replace_in === e_REQUIRE) && (i.U === io.curNode_in))){
      io.replace_out := e_RANDINV
    }
  }
  
}

class rule_CRIC(i:Int,j:Int,NUM_NODE:Int,NUM_CACHE:Int,NUM_ADDR:Int,NUM_DATA:Int,NUM_LOCK:Int) extends rule(NUM_NODE,NUM_CACHE,NUM_ADDR,NUM_DATA,NUM_LOCK){


  when(io.en_r){
  
    when(((io.replace_in === e_RANDINV) && ((i.U === io.curNode_in) && (io.node_in(i).cache(j).state === e_INVALID)))){
      io.curCache_out := j.U
      io.replace_out := e_DONE
    }
  }
  
}

class rule_RNI(i:Int,NUM_NODE:Int,NUM_CACHE:Int,NUM_ADDR:Int,NUM_DATA:Int,NUM_LOCK:Int) extends rule(NUM_NODE,NUM_CACHE,NUM_ADDR,NUM_DATA,NUM_LOCK){


  when(io.en_r){
    var forall_flag = true.B
    for(j <- 0 until NUM_CACHE){
    when(!((io.node_in(i).cache(j).state =/= e_INVALID))){
      forall_flag = false.B
    }
    }
    when(((io.replace_in === e_REQUIRE) && ((i.U === io.curNode_in) && forall_flag))){
      io.replace_out := e_RANDOM
    }
  }
  
}

class rule_CRC(i:Int,NUM_NODE:Int,NUM_CACHE:Int,NUM_ADDR:Int,NUM_DATA:Int,NUM_LOCK:Int) extends rule(NUM_NODE,NUM_CACHE,NUM_ADDR,NUM_DATA,NUM_LOCK){


  when(io.en_r){
  
    when((io.replace_in === e_RANDOM)){
      io.curCache_out := i.U
      io.replace_out := e_DESIGNATED
    }
  }
  
}

class rule_DCND(i:Int,j:Int,NUM_NODE:Int,NUM_CACHE:Int,NUM_ADDR:Int,NUM_DATA:Int,NUM_LOCK:Int) extends rule(NUM_NODE,NUM_CACHE,NUM_ADDR,NUM_DATA,NUM_LOCK){


  when(io.en_r){
  
    when(((io.replace_in === e_DESIGNATED) && ((i.U === io.curNode_in) && ((j.U === io.curCache_in) && (io.node_in(i).cache(j).state =/= e_DIRTY))))){
      io.replace_out := e_DONE
    }
  }
  
}

class rule_DCD(i:Int,j:Int,NUM_NODE:Int,NUM_CACHE:Int,NUM_ADDR:Int,NUM_DATA:Int,NUM_LOCK:Int) extends rule(NUM_NODE,NUM_CACHE,NUM_ADDR,NUM_DATA,NUM_LOCK){


  when(io.en_r){
  
    when(((io.replace_in === e_DESIGNATED) && ((i.U === io.curNode_in) && ((j.U === io.curCache_in) && (io.node_in(i).cache(j).state === e_DIRTY))))){
      io.replace_out := e_TOREP
    }
  }
  
}

class rule_Replace(i:Int,j:Int,a:Int,NUM_NODE:Int,NUM_CACHE:Int,NUM_ADDR:Int,NUM_DATA:Int,NUM_LOCK:Int) extends rule(NUM_NODE,NUM_CACHE,NUM_ADDR,NUM_DATA,NUM_LOCK){


  when(io.en_r){
  
    when(((io.replace_in === e_TOREP) && ((i.U === io.curNode_in) && ((j.U === io.curCache_in) && (a.U === io.node_in(i).cache(j).addr))))){
      io.memory_out(a).data := io.node_out(i).cache(j).data
      io.node_out(i).cache(j).state := e_INVALID
      io.replace_out := e_DONE
    }
  }
  
}

class rule_RepAll(i:Int,j:Int,a:Int,NUM_NODE:Int,NUM_CACHE:Int,NUM_ADDR:Int,NUM_DATA:Int,NUM_LOCK:Int) extends rule(NUM_NODE,NUM_CACHE,NUM_ADDR,NUM_DATA,NUM_LOCK){


  when(io.en_r){
  
    when(((io.replace_in === e_REQREPALL) && ((io.node_in(i).cache(j).state === e_DIRTY) && (io.node_in(i).cache(j).addr === a.U)))){
      io.memory_out(a).data := io.node_out(i).cache(j).data
      io.node_out(i).cache(j).state := e_INVALID
    }
  }
  
}

class rule_RepAllDone(NUM_NODE:Int,NUM_CACHE:Int,NUM_ADDR:Int,NUM_DATA:Int,NUM_LOCK:Int) extends rule(NUM_NODE,NUM_CACHE,NUM_ADDR,NUM_DATA,NUM_LOCK){


  when(io.en_r){
    var forall_flag = true.B
    for(i <- 0 until NUM_NODE){
    when(!((io.node_in(i).cache(j).state =/= e_DIRTY))){
      forall_flag = false.B
    }
    }
    when(((io.replace_in === e_REQREPALL) && forall_flag)){
      io.replace_out := e_REPALLDONE
    }
  }
  
}

class rule_NLNCRR(i:Int,a:Int,NUM_NODE:Int,NUM_CACHE:Int,NUM_ADDR:Int,NUM_DATA:Int,NUM_LOCK:Int) extends rule(NUM_NODE,NUM_CACHE,NUM_ADDR,NUM_DATA,NUM_LOCK){


  when(io.en_r){
    var forall_flag = true.B
    for(j <- 0 until NUM_CACHE){
    when(!(((io.node_in(i).cache(j).state === e_INVALID) || (io.node_in(i).cache(j).addr =/= a.U)))){
      forall_flag = false.B
    }
    }
    when(((io.replace_in === e_NON) && (!(io.node_in(i).hasLock) && forall_flag))){
      io.curNode_out := i.U
      io.curMemory_out := a.U
      io.replace_out := e_REQUIRE
      io.repRule_out := e_NLNCR
    }
  }
  
}

class rule_NLNCRD(i:Int,j:Int,a:Int,NUM_NODE:Int,NUM_CACHE:Int,NUM_ADDR:Int,NUM_DATA:Int,NUM_LOCK:Int) extends rule(NUM_NODE,NUM_CACHE,NUM_ADDR,NUM_DATA,NUM_LOCK){


  when(io.en_r){
  
    when(((io.replace_in === e_DONE) && ((io.repRule_in === e_NLNCR) && ((i.U === io.curNode_in) && ((j.U === io.curCache_in) && (a.U === io.curMemory_in)))))){
      io.node_out(i).cache(j).addr := a.U
      io.node_out(i).cache(j).data := io.memory_out(a).data
      io.node_out(i).cache(j).state := e_VALID
      io.replace_out := e_NON
      io.repRule_out := e_NONE
    }
  }
  
}

class rule_NLCW(i:Int,j:Int,a:Int,d:Int,NUM_NODE:Int,NUM_CACHE:Int,NUM_ADDR:Int,NUM_DATA:Int,NUM_LOCK:Int) extends rule(NUM_NODE,NUM_CACHE,NUM_ADDR,NUM_DATA,NUM_LOCK){


  when(io.en_r){
    var forall_flag = true.B
    for(l <- 0 until NUM_LOCK){
    when(!(!(io.lock_in(l).inProtection(a)))){
      forall_flag = false.B
    }
    }
    when(((io.replace_in === e_NON) && (!(io.node_in(i).hasLock) && ((io.node_in(i).cache(j).state =/= e_INVALID) && ((io.node_in(i).cache(j).addr === a.U) && forall_flag))))){
      io.node_out(i).cache(j).data := d.U
      io.node_out(i).cache(j).state := e_DIRTY
    }
  }
  
}

class rule_NLNCWR(i:Int,a:Int,d:Int,NUM_NODE:Int,NUM_CACHE:Int,NUM_ADDR:Int,NUM_DATA:Int,NUM_LOCK:Int) extends rule(NUM_NODE,NUM_CACHE,NUM_ADDR,NUM_DATA,NUM_LOCK){


  when(io.en_r){
    var forall_flag = true.B
    for(j <- 0 until NUM_CACHE){
    when(!(((io.node_in(i).cache(j).state === e_INVALID) || (io.node_in(i).cache(j).addr =/= a.U)))){
      forall_flag = false.B
    }
    }
    var forall_flag = true.B
    for(l <- 0 until NUM_LOCK){
    when(!(!(io.lock_in(l).inProtection(a)))){
      forall_flag = false.B
    }
    }
    when(((io.replace_in === e_NON) && (!(io.node_in(i).hasLock) && (forall_flag && forall_flag)))){
      io.curNode_out := i.U
      io.curMemory_out := a.U
      io.curData_out := d.U
      io.replace_out := e_REQUIRE
      io.repRule_out := e_NLNCW
    }
  }
  
}

class rule_NLNCWD(i:Int,j:Int,a:Int,d:Int,NUM_NODE:Int,NUM_CACHE:Int,NUM_ADDR:Int,NUM_DATA:Int,NUM_LOCK:Int) extends rule(NUM_NODE,NUM_CACHE,NUM_ADDR,NUM_DATA,NUM_LOCK){


  when(io.en_r){
  
    when(((io.replace_in === e_DONE) && ((io.repRule_in === e_NLNCW) && ((i.U === io.curNode_in) && ((j.U === io.curCache_in) && ((a.U === io.curMemory_in) && (d.U === io.curData_in))))))){
      io.node_out(i).cache(j).addr := a.U
      io.node_out(i).cache(j).data := d.U
      io.node_out(i).cache(j).state := e_DIRTY
      io.replace_out := e_NON
      io.repRule_out := e_NONE
    }
  }
  
}

class rule_LCFRRA(i:Int,j:Int,a:Int,l:Int,NUM_NODE:Int,NUM_CACHE:Int,NUM_ADDR:Int,NUM_DATA:Int,NUM_LOCK:Int) extends rule(NUM_NODE,NUM_CACHE,NUM_ADDR,NUM_DATA,NUM_LOCK){


  when(io.en_r){
  
    when(((io.replace_in === e_NON) && (io.node_in(i).hasLock && (io.lock_in(l).beUsed && ((io.lock_in(l).owner === i.U) && (io.node_in(i).firstRead(a) && ((io.node_in(i).cache(j).state =/= e_INVALID) && (io.node_in(i).cache(j).addr === a.U)))))))){
      io.curNode_out := i.U
      io.curCache_out := j.U
      io.curMemory_out := a.U
      io.curLock_out := l.U
      io.replace_out := e_REQREPALL
      io.repRule_out := e_LCFR
    }
  }
  
}

class rule_LCFRAD(NUM_NODE:Int,NUM_CACHE:Int,NUM_ADDR:Int,NUM_DATA:Int,NUM_LOCK:Int) extends rule(NUM_NODE,NUM_CACHE,NUM_ADDR,NUM_DATA,NUM_LOCK){


  when(io.en_r){
  
    when(((io.replace_in === e_REPALLDONE) && (io.repRule_in === e_LCFR))){
      io.replace_out := e_DESIGNATED
    }
  }
  
}

class rule_LCFRD(i:Int,j:Int,a:Int,l:Int,NUM_NODE:Int,NUM_CACHE:Int,NUM_ADDR:Int,NUM_DATA:Int,NUM_LOCK:Int) extends rule(NUM_NODE,NUM_CACHE,NUM_ADDR,NUM_DATA,NUM_LOCK){


  when(io.en_r){
  
    when(((io.replace_in === e_DONE) && ((io.repRule_in === e_LCFR) && ((i.U === io.curNode_in) && ((j.U === io.curCache_in) && ((a.U === io.curMemory_in) && (l.U === io.curLock_in))))))){
      io.node_out(i).cache(j).data := io.memory_out(a).data
      io.node_out(i).cache(j).state := e_VALID
      io.node_out(i).firstRead(a) := false.B
      io.lock_out(l).inProtection(a) := true.B
      io.replace_out := e_NON
      io.repRule_out := e_NONE
    }
  }
  
}

class rule_LNCFRRA(i:Int,a:Int,l:Int,NUM_NODE:Int,NUM_CACHE:Int,NUM_ADDR:Int,NUM_DATA:Int,NUM_LOCK:Int) extends rule(NUM_NODE,NUM_CACHE,NUM_ADDR,NUM_DATA,NUM_LOCK){


  when(io.en_r){
    var forall_flag = true.B
    for(j <- 0 until NUM_CACHE){
    when(!(((io.node_in(i).cache(j).state === e_INVALID) || (io.node_in(i).cache(j).addr =/= a.U)))){
      forall_flag = false.B
    }
    }
    when(((io.replace_in === e_NON) && (io.node_in(i).hasLock && (io.lock_in(l).beUsed && ((io.lock_in(l).owner === i.U) && (io.node_in(i).firstRead(a) && forall_flag)))))){
      io.curNode_out := i.U
      io.curMemory_out := a.U
      io.curLock_out := l.U
      io.replace_out := e_REQREPALL
      io.repRule_out := e_LNCFR
    }
  }
  
}

class rule_LNCFRAD(NUM_NODE:Int,NUM_CACHE:Int,NUM_ADDR:Int,NUM_DATA:Int,NUM_LOCK:Int) extends rule(NUM_NODE,NUM_CACHE,NUM_ADDR,NUM_DATA,NUM_LOCK){


  when(io.en_r){
  
    when(((io.replace_in === e_REPALLDONE) && (io.repRule_in === e_LNCFR))){
      io.replace_out := e_REQUIRE
    }
  }
  
}

class rule_LNCFRD(i:Int,j:Int,a:Int,l:Int,NUM_NODE:Int,NUM_CACHE:Int,NUM_ADDR:Int,NUM_DATA:Int,NUM_LOCK:Int) extends rule(NUM_NODE,NUM_CACHE,NUM_ADDR,NUM_DATA,NUM_LOCK){


  when(io.en_r){
  
    when(((io.replace_in === e_DONE) && ((io.repRule_in === e_LNCFR) && ((i.U === io.curNode_in) && ((j.U === io.curCache_in) && ((a.U === io.curMemory_in) && (l.U === io.curLock_in))))))){
      io.node_out(i).cache(j).addr := a.U
      io.node_out(i).cache(j).data := io.memory_out(a).data
      io.node_out(i).cache(j).state := e_VALID
      io.node_out(i).firstRead(a) := false.B
      io.lock_out(l).inProtection(a) := true.B
      io.replace_out := e_NON
      io.repRule_out := e_NONE
    }
  }
  
}

class rule_LNCNFRR(i:Int,a:Int,l:Int,NUM_NODE:Int,NUM_CACHE:Int,NUM_ADDR:Int,NUM_DATA:Int,NUM_LOCK:Int) extends rule(NUM_NODE,NUM_CACHE,NUM_ADDR,NUM_DATA,NUM_LOCK){


  when(io.en_r){
    var forall_flag = true.B
    for(j <- 0 until NUM_CACHE){
    when(!(((io.node_in(i).cache(j).state === e_INVALID) || (io.node_in(i).cache(j).addr =/= a.U)))){
      forall_flag = false.B
    }
    }
    when(((io.replace_in === e_NON) && (io.node_in(i).hasLock && (io.lock_in(l).beUsed && ((io.lock_in(l).owner === i.U) && (!(io.node_in(i).firstRead(a)) && forall_flag)))))){
      io.curNode_out := i.U
      io.curMemory_out := a.U
      io.curLock_out := l.U
      io.replace_out := e_REQUIRE
      io.repRule_out := e_LNCNFR
    }
  }
  
}

class rule_LNCNFRD(i:Int,j:Int,a:Int,l:Int,NUM_NODE:Int,NUM_CACHE:Int,NUM_ADDR:Int,NUM_DATA:Int,NUM_LOCK:Int) extends rule(NUM_NODE,NUM_CACHE,NUM_ADDR,NUM_DATA,NUM_LOCK){


  when(io.en_r){
  
    when(((io.replace_in === e_DONE) && ((io.repRule_in === e_LNCNFR) && ((i.U === io.curNode_in) && ((j.U === io.curCache_in) && ((a.U === io.curMemory_in) && (l.U === io.curLock_in))))))){
      io.node_out(i).cache(j).addr := a.U
      io.node_out(i).cache(j).data := io.memory_out(a).data
      io.node_out(i).cache(j).state := e_VALID
      io.lock_out(l).inProtection(a) := true.B
      io.replace_out := e_NON
      io.repRule_out := e_NONE
    }
  }
  
}

class rule_LCW(i:Int,j:Int,a:Int,d:Int,l:Int,NUM_NODE:Int,NUM_CACHE:Int,NUM_ADDR:Int,NUM_DATA:Int,NUM_LOCK:Int) extends rule(NUM_NODE,NUM_CACHE,NUM_ADDR,NUM_DATA,NUM_LOCK){


  when(io.en_r){
    var forall_flag = true.B
    for(m <- 0 until NUM_LOCK){
    when(!((!(io.lock_in(m).inProtection(a))||((m.U === l.U))))){
      forall_flag = false.B
    }
    }
    when(((io.replace_in === e_NON) && (io.node_in(i).hasLock && (io.lock_in(l).beUsed && ((io.lock_in(l).owner === i.U) && ((io.node_in(i).cache(j).state =/= e_INVALID) && ((io.node_in(i).cache(j).addr === a.U) && forall_flag))))))){
      io.memory_out(a).data := d.U
      io.node_out(i).cache(j).data := d.U
      io.node_out(i).cache(j).state := e_VALID
      io.lock_out(l).inProtection(a) := true.B
    }
  }
  
}

class rule_LNCW(i:Int,a:Int,d:Int,l:Int,NUM_NODE:Int,NUM_CACHE:Int,NUM_ADDR:Int,NUM_DATA:Int,NUM_LOCK:Int) extends rule(NUM_NODE,NUM_CACHE,NUM_ADDR,NUM_DATA,NUM_LOCK){


  when(io.en_r){
    var forall_flag = true.B
    for(j <- 0 until NUM_CACHE){
    when(!(((io.node_in(i).cache(j).state === e_INVALID) || (io.node_in(i).cache(j).addr =/= a.U)))){
      forall_flag = false.B
    }
    }
    var forall_flag = true.B
    for(m <- 0 until NUM_LOCK){
    when(!((!(io.lock_in(m).inProtection(a))||((m.U === l.U))))){
      forall_flag = false.B
    }
    }
    when(((io.replace_in === e_NON) && (io.node_in(i).hasLock && (io.lock_in(l).beUsed && ((io.lock_in(l).owner === i.U) && (forall_flag && forall_flag)))))){
      io.memory_out(a).data := d.U
      io.lock_out(l).inProtection(a) := true.B
    }
  }
  
}

class rule_Acquire(i:Int,l:Int,NUM_NODE:Int,NUM_CACHE:Int,NUM_ADDR:Int,NUM_DATA:Int,NUM_LOCK:Int) extends rule(NUM_NODE,NUM_CACHE,NUM_ADDR,NUM_DATA,NUM_LOCK){


  when(io.en_r){
  
    when(((io.replace_in === e_NON) && (!(io.node_in(i).hasLock) && !(io.lock_in(l).beUsed)))){
      io.lock_out(l).beUsed := true.B
      io.lock_out(l).owner := i.U
      io.node_out(i).hasLock := true.B
      for( j <- 0 until NUM_ADDR){
        io.node_out(i).firstRead(j) := true.B
      }
    }
  }
  
}

class rule_Release(i:Int,l:Int,NUM_NODE:Int,NUM_CACHE:Int,NUM_ADDR:Int,NUM_DATA:Int,NUM_LOCK:Int) extends rule(NUM_NODE,NUM_CACHE,NUM_ADDR,NUM_DATA,NUM_LOCK){


  when(io.en_r){
  
    when(((io.replace_in === e_NON) && (io.node_in(i).hasLock && (io.lock_in(l).beUsed && (io.lock_in(l).owner === i.U))))){
      io.lock_out(l).beUsed := false.B
      io.node_out(i).hasLock := false.B
      for( a <- 0 until NUM_ADDR){
        io.lock_out(l).inProtection(a) := false.B
      }
    }
  }
  
}

class godsont(NUM_NODE:Int,NUM_CACHE:Int,NUM_ADDR:Int,NUM_DATA:Int,NUM_LOCK:Int) extends Module{

  val e_INVALID::e_DIRTY::e_VALID::Nil = Enum(3)
  val e_NON::e_REQUIRE::e_REQREPALL::e_RANDOM::e_RANDINV::e_DESIGNATED::e_TOREP::e_DONE::e_REPALLDONE::Nil = Enum(9)
  val e_NONE::e_NLNCR::e_NLNCW::e_LNCFR::e_LCFR::e_LNCNFR::Nil = Enum(6)

  val io = IO(new Bundle {
    val en_a = Input(UInt(log2Ceil(2*NUM_NODE+3*NUM_NODE*NUM_CACHE+1*NUM_CACHE+3*NUM_NODE*NUM_CACHE*NUM_ADDR+1*NUM_NODE*NUM_ADDR+2*NUM_NODE*NUM_CACHE*NUM_ADDR*NUM_DATA+1*NUM_NODE*NUM_ADDR*NUM_DATA+4*NUM_NODE*NUM_CACHE*NUM_ADDR*NUM_LOCK+2*NUM_NODE*NUM_ADDR*NUM_LOCK+1*NUM_NODE*NUM_CACHE*NUM_ADDR*NUM_DATA*NUM_LOCK+1*NUM_NODE*NUM_ADDR*NUM_DATA*NUM_LOCK+2*NUM_NODE*NUM_LOCK+3).W))
    val memory_out = Output(Vec(NUM_ADDR,new MEMORY(NUM_NODE,NUM_CACHE,NUM_ADDR,NUM_DATA,NUM_LOCK)))
    val lock_out = Output(Vec(NUM_LOCK,new LOCK(NUM_NODE,NUM_CACHE,NUM_ADDR,NUM_DATA,NUM_LOCK)))
    val node_out = Output(Vec(NUM_NODE,new NODE(NUM_NODE,NUM_CACHE,NUM_ADDR,NUM_DATA,NUM_LOCK)))
    val curNode_out = Output(UInt(log2Ceil(NUM_NODE).W))
    val curCache_out = Output(UInt(log2Ceil(NUM_CACHE).W))
    val curMemory_out = Output(UInt(log2Ceil(NUM_ADDR).W))
    val curData_out = Output(UInt(log2Ceil(NUM_DATA).W))
    val curLock_out = Output(UInt(log2Ceil(NUM_LOCK).W))
    val replace_out = Output(UInt(4.W))
    val repRule_out = Output(UInt(3.W))
  )
  
  val memory_init = Wire(Vec(NUM_ADDR,new MEMORY(NUM_NODE,NUM_CACHE,NUM_ADDR,NUM_DATA,NUM_LOCK)))
  val memory_reg = RegInit(memory_init)
  io.memory_out:=memory_reg
  val lock_init = Wire(Vec(NUM_LOCK,new LOCK(NUM_NODE,NUM_CACHE,NUM_ADDR,NUM_DATA,NUM_LOCK)))
  val lock_reg = RegInit(lock_init)
  io.lock_out:=lock_reg
  val node_init = Wire(Vec(NUM_NODE,new NODE(NUM_NODE,NUM_CACHE,NUM_ADDR,NUM_DATA,NUM_LOCK)))
  val node_reg = RegInit(node_init)
  io.node_out:=node_reg
  val curNode_init = Wire(UInt(log2Ceil(NUM_NODE).W))
  val curNode_reg = RegInit(curNode_init)
  io.curNode_out:=curNode_reg
  val curCache_init = Wire(UInt(log2Ceil(NUM_CACHE).W))
  val curCache_reg = RegInit(curCache_init)
  io.curCache_out:=curCache_reg
  val curMemory_init = Wire(UInt(log2Ceil(NUM_ADDR).W))
  val curMemory_reg = RegInit(curMemory_init)
  io.curMemory_out:=curMemory_reg
  val curData_init = Wire(UInt(log2Ceil(NUM_DATA).W))
  val curData_reg = RegInit(curData_init)
  io.curData_out:=curData_reg
  val curLock_init = Wire(UInt(log2Ceil(NUM_LOCK).W))
  val curLock_reg = RegInit(curLock_init)
  io.curLock_out:=curLock_reg
  val replace_init = Wire(UInt(4.W))
  val replace_reg = RegInit(replace_init)
  io.replace_out:=replace_reg
  val repRule_init = Wire(UInt(3.W))
  val repRule_reg = RegInit(repRule_init)
  io.repRule_out:=repRule_reg
  
  for( i <- 0 until NUM_NODE){
    for( j <- 0 until NUM_CACHE){
      node_init(i).cache(j).state := e_INVALID
    }
    node_init(i).hasLock := false.B
    for( a <- 0 until NUM_ADDR){
      node_init(i).firstRead(a) := true.B
    }
    curNode_init := 0.U
  }
  for( j <- 0 until NUM_CACHE){
    curCache_init := 0.U
  }
  for( a <- 0 until NUM_ADDR){
    memory_init(a).data := 0.U
    curMemory_init := 0.U
  }
  curData_init := 0.U
  for( l <- 0 until NUM_LOCK){
    lock_init(l).beUsed := false.B
    curLock_init := 0.U
    for( a <- 0 until NUM_ADDR){
      lock_init(l).inProtection(a) := false.B
    }
  }
  replace_init := e_NON
  repRule_init := e_NONE
  
  var rules = ArrayBuffer[rule]()
  var index = 0
  for(i <- 0 until NUM_NODE) {
    rules += Module(new rule_RI(i, NUM_NODE,NUM_CACHE,NUM_ADDR,NUM_DATA,NUM_LOCK))
  }
  
  for(i <- 0 until NUM_NODE) {
    for(j <- 0 until NUM_CACHE) {
      rules += Module(new rule_CRIC(i,j, NUM_NODE,NUM_CACHE,NUM_ADDR,NUM_DATA,NUM_LOCK))
    }
  }
  
  for(i <- 0 until NUM_NODE) {
    rules += Module(new rule_RNI(i, NUM_NODE,NUM_CACHE,NUM_ADDR,NUM_DATA,NUM_LOCK))
  }
  
  for(i <- 0 until NUM_CACHE) {
    rules += Module(new rule_CRC(i, NUM_NODE,NUM_CACHE,NUM_ADDR,NUM_DATA,NUM_LOCK))
  }
  
  for(i <- 0 until NUM_NODE) {
    for(j <- 0 until NUM_CACHE) {
      rules += Module(new rule_DCND(i,j, NUM_NODE,NUM_CACHE,NUM_ADDR,NUM_DATA,NUM_LOCK))
    }
  }
  
  for(i <- 0 until NUM_NODE) {
    for(j <- 0 until NUM_CACHE) {
      rules += Module(new rule_DCD(i,j, NUM_NODE,NUM_CACHE,NUM_ADDR,NUM_DATA,NUM_LOCK))
    }
  }
  
  for(i <- 0 until NUM_NODE) {
    for(j <- 0 until NUM_CACHE) {
      for(a <- 0 until NUM_ADDR) {
        rules += Module(new rule_Replace(i,j,a, NUM_NODE,NUM_CACHE,NUM_ADDR,NUM_DATA,NUM_LOCK))
      }
    }
  }
  
  for(i <- 0 until NUM_NODE) {
    for(j <- 0 until NUM_CACHE) {
      for(a <- 0 until NUM_ADDR) {
        rules += Module(new rule_RepAll(i,j,a, NUM_NODE,NUM_CACHE,NUM_ADDR,NUM_DATA,NUM_LOCK))
      }
    }
  }
  
  rules += Module(new rule_RepAllDone(NUM_NODE,NUM_CACHE,NUM_ADDR,NUM_DATA,NUM_LOCK))
  for(i <- 0 until NUM_NODE) {
    for(a <- 0 until NUM_ADDR) {
      rules += Module(new rule_NLNCRR(i,a, NUM_NODE,NUM_CACHE,NUM_ADDR,NUM_DATA,NUM_LOCK))
    }
  }
  
  for(i <- 0 until NUM_NODE) {
    for(j <- 0 until NUM_CACHE) {
      for(a <- 0 until NUM_ADDR) {
        rules += Module(new rule_NLNCRD(i,j,a, NUM_NODE,NUM_CACHE,NUM_ADDR,NUM_DATA,NUM_LOCK))
      }
    }
  }
  
  for(i <- 0 until NUM_NODE) {
    for(j <- 0 until NUM_CACHE) {
      for(a <- 0 until NUM_ADDR) {
        for(d <- 0 until NUM_DATA) {
          rules += Module(new rule_NLCW(i,j,a,d, NUM_NODE,NUM_CACHE,NUM_ADDR,NUM_DATA,NUM_LOCK))
        }
      }
    }
  }
  
  for(i <- 0 until NUM_NODE) {
    for(a <- 0 until NUM_ADDR) {
      for(d <- 0 until NUM_DATA) {
        rules += Module(new rule_NLNCWR(i,a,d, NUM_NODE,NUM_CACHE,NUM_ADDR,NUM_DATA,NUM_LOCK))
      }
    }
  }
  
  for(i <- 0 until NUM_NODE) {
    for(j <- 0 until NUM_CACHE) {
      for(a <- 0 until NUM_ADDR) {
        for(d <- 0 until NUM_DATA) {
          rules += Module(new rule_NLNCWD(i,j,a,d, NUM_NODE,NUM_CACHE,NUM_ADDR,NUM_DATA,NUM_LOCK))
        }
      }
    }
  }
  
  for(i <- 0 until NUM_NODE) {
    for(j <- 0 until NUM_CACHE) {
      for(a <- 0 until NUM_ADDR) {
        for(l <- 0 until NUM_LOCK) {
          rules += Module(new rule_LCFRRA(i,j,a,l, NUM_NODE,NUM_CACHE,NUM_ADDR,NUM_DATA,NUM_LOCK))
        }
      }
    }
  }
  
  rules += Module(new rule_LCFRAD(NUM_NODE,NUM_CACHE,NUM_ADDR,NUM_DATA,NUM_LOCK))
  for(i <- 0 until NUM_NODE) {
    for(j <- 0 until NUM_CACHE) {
      for(a <- 0 until NUM_ADDR) {
        for(l <- 0 until NUM_LOCK) {
          rules += Module(new rule_LCFRD(i,j,a,l, NUM_NODE,NUM_CACHE,NUM_ADDR,NUM_DATA,NUM_LOCK))
        }
      }
    }
  }
  
  for(i <- 0 until NUM_NODE) {
    for(a <- 0 until NUM_ADDR) {
      for(l <- 0 until NUM_LOCK) {
        rules += Module(new rule_LNCFRRA(i,a,l, NUM_NODE,NUM_CACHE,NUM_ADDR,NUM_DATA,NUM_LOCK))
      }
    }
  }
  
  rules += Module(new rule_LNCFRAD(NUM_NODE,NUM_CACHE,NUM_ADDR,NUM_DATA,NUM_LOCK))
  for(i <- 0 until NUM_NODE) {
    for(j <- 0 until NUM_CACHE) {
      for(a <- 0 until NUM_ADDR) {
        for(l <- 0 until NUM_LOCK) {
          rules += Module(new rule_LNCFRD(i,j,a,l, NUM_NODE,NUM_CACHE,NUM_ADDR,NUM_DATA,NUM_LOCK))
        }
      }
    }
  }
  
  for(i <- 0 until NUM_NODE) {
    for(a <- 0 until NUM_ADDR) {
      for(l <- 0 until NUM_LOCK) {
        rules += Module(new rule_LNCNFRR(i,a,l, NUM_NODE,NUM_CACHE,NUM_ADDR,NUM_DATA,NUM_LOCK))
      }
    }
  }
  
  for(i <- 0 until NUM_NODE) {
    for(j <- 0 until NUM_CACHE) {
      for(a <- 0 until NUM_ADDR) {
        for(l <- 0 until NUM_LOCK) {
          rules += Module(new rule_LNCNFRD(i,j,a,l, NUM_NODE,NUM_CACHE,NUM_ADDR,NUM_DATA,NUM_LOCK))
        }
      }
    }
  }
  
  for(i <- 0 until NUM_NODE) {
    for(j <- 0 until NUM_CACHE) {
      for(a <- 0 until NUM_ADDR) {
        for(d <- 0 until NUM_DATA) {
          for(l <- 0 until NUM_LOCK) {
            rules += Module(new rule_LCW(i,j,a,d,l, NUM_NODE,NUM_CACHE,NUM_ADDR,NUM_DATA,NUM_LOCK))
          }
        }
      }
    }
  }
  
  for(i <- 0 until NUM_NODE) {
    for(a <- 0 until NUM_ADDR) {
      for(d <- 0 until NUM_DATA) {
        for(l <- 0 until NUM_LOCK) {
          rules += Module(new rule_LNCW(i,a,d,l, NUM_NODE,NUM_CACHE,NUM_ADDR,NUM_DATA,NUM_LOCK))
        }
      }
    }
  }
  
  for(i <- 0 until NUM_NODE) {
    for(l <- 0 until NUM_LOCK) {
      rules += Module(new rule_Acquire(i,l, NUM_NODE,NUM_CACHE,NUM_ADDR,NUM_DATA,NUM_LOCK))
      rules += Module(new rule_Release(i,l, NUM_NODE,NUM_CACHE,NUM_ADDR,NUM_DATA,NUM_LOCK))
    }
  }
  
  for(i <- 0 until 2*NUM_NODE+3*NUM_NODE*NUM_CACHE+1*NUM_CACHE+3*NUM_NODE*NUM_CACHE*NUM_ADDR+1*NUM_NODE*NUM_ADDR+2*NUM_NODE*NUM_CACHE*NUM_ADDR*NUM_DATA+1*NUM_NODE*NUM_ADDR*NUM_DATA+4*NUM_NODE*NUM_CACHE*NUM_ADDR*NUM_LOCK+2*NUM_NODE*NUM_ADDR*NUM_LOCK+1*NUM_NODE*NUM_CACHE*NUM_ADDR*NUM_DATA*NUM_LOCK+1*NUM_NODE*NUM_ADDR*NUM_DATA*NUM_LOCK+2*NUM_NODE*NUM_LOCK+3) {
    rules(i).io.memory_in := memory_reg
    rules(i).io.lock_in := lock_reg
    rules(i).io.node_in := node_reg
    rules(i).io.curNode_in := curNode_reg
    rules(i).io.curCache_in := curCache_reg
    rules(i).io.curMemory_in := curMemory_reg
    rules(i).io.curData_in := curData_reg
    rules(i).io.curLock_in := curLock_reg
    rules(i).io.replace_in := replace_reg
    rules(i).io.repRule_in := repRule_reg
    rules(i).io.en_r:=(io.en_a=== index.U)
    when(io.en_a=== index.U){
      memory_reg := rules(i).io.memory_out 
      lock_reg := rules(i).io.lock_out 
      node_reg := rules(i).io.node_out 
      curNode_reg := rules(i).io.curNode_out 
      curCache_reg := rules(i).io.curCache_out 
      curMemory_reg := rules(i).io.curMemory_out 
      curData_reg := rules(i).io.curData_out 
      curLock_reg := rules(i).io.curLock_out 
      replace_reg := rules(i).io.replace_out 
      repRule_reg := rules(i).io.repRule_out 
    }
    index = index +1 
  }
  
}

