import chisel3._
import chisel3.util._
import scala.collection.mutable.ArrayBuffer

class rule(clientNUMS:Int) extends Module{

  val e_I::e_T::e_C::e_E::Nil = Enum(4)

  val io = IO(new Bundle {
    val en_r = Input(Bool())
    val n_in = Input(Vec(clientNUMS,UInt(2.W)))
    val n_out = Output(Vec(clientNUMS,UInt(2.W)))
    val x_in = Input(Bool()) 
    val x_out = Output(Bool()) 
  )
  
  io.n_out:=io.n_in
  io.x_out:=io.x_in
  
}

class rule_Try(i:Int,clientNUMS:Int) extends rule(clientNUMS){


  when(io.en_r){
      when((io.n_in(i) === e_I)){
        io.n_out(i) := e_T
        }
  }
  
}

class rule_Crit(i:Int,clientNUMS:Int) extends rule(clientNUMS){


  when(io.en_r){
      when(((io.n_in(i) === e_T) && (io.x_in === true.B))){
        io.n_out(i) := e_C
          io.x_out := false.B
        }
  }
  
}

class rule_Exit(i:Int,clientNUMS:Int) extends rule(clientNUMS){


  when(io.en_r){
      when((io.n_in(i) === e_C)){
        io.n_out(i) := e_E
        }
  }
  
}

class rule_Idle(i:Int,clientNUMS:Int) extends rule(clientNUMS){


  when(io.en_r){
      when((io.n_in(i) === e_E)){
        io.n_out(i) := e_I
          io.x_out := true.B
        }
  }
  
}

class mutual(clientNUMS:Int) extends Module{

  val e_I::e_T::e_C::e_E::Nil = Enum(4)

  val io = IO(new Bundle {
    val en_a = Input(UInt(log2Ceil(4*clientNUMS+0).W))
    val n_out = Output(Vec(clientNUMS,UInt(2.W)))
    val x_out = Output(Bool()) 
  )
  
  val n_init = Wire(Vec(clientNUMS,UInt(2.W)))
  val n_reg = RegInit(n_init)
  io.n_out:=n_reg
  val x_init = Wire(Bool())
  val x_reg = RegInit(x_init)
  io.x_out:=x_reg
  
  for( i <- 0 until clientNUMS){
    n_init(i) := e_I
    }
  x_init := true.B
  
  var rules = ArrayBuffer[rule]()
  var index = 0
  for(i <- 0 until clientNUMS) {
    rules += Module(new rule_Try(i, clientNUMS))
      rules += Module(new rule_Crit(i, clientNUMS))
      rules += Module(new rule_Exit(i, clientNUMS))
      rules += Module(new rule_Idle(i, clientNUMS))
    }
  
  for(i <- 0 until 4*clientNUMS+0) {
    rules(i).io.n_in := n_reg
      rules(i).io.x_in := x_reg
      rules(i).io.en_r:=(io.en_a=== index.U)
    when(io.en_a=== index.U){
      n_reg := rules(i).io.n_out 
      x_reg := rules(i).io.x_out 
      }
      index = index +1 
    }
  
}

