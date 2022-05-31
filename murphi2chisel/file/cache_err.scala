import chisel3._
import chisel3.util._
import scala.collection.mutable.ArrayBuffer

class rule(num_clients:Int) extends Module{

  val e_empty::e_req_shared::e_req_exclusive::e_invalidate::e_invalidate_ack::e_grant_shared::e_grant_exclusive::Nil = Enum(7)
  val e_invalid::e_shared::e_exclusive::Nil = Enum(3)

  val io = IO(new Bundle {
    val en_r = Input(Bool())
    val channel1_in = Input(Vec(num_clients,UInt(3.W)))
    val channel1_out = Output(Vec(num_clients,UInt(3.W)))
    val channel2_4_in = Input(Vec(num_clients,UInt(3.W)))
    val channel2_4_out = Output(Vec(num_clients,UInt(3.W)))
    val channel3_in = Input(Vec(num_clients,UInt(3.W)))
    val channel3_out = Output(Vec(num_clients,UInt(3.W)))
    val home_sharer_list_in = Input(Vec(num_clients,Bool()))
    val home_sharer_list_out = Output(Vec(num_clients,Bool()))
    val home_invalidate_list_in = Input(Vec(num_clients,Bool()))
    val home_invalidate_list_out = Output(Vec(num_clients,Bool()))
    val home_exclusive_granted_in = Input(Bool()) 
    val home_exclusive_granted_out = Output(Bool()) 
    val home_current_command_in = Input(UInt(3.W))
    val home_current_command_out = Output(UInt(3.W))
    val home_current_client_in = Input(UInt(log2Ceil(num_clients).W))
    val home_current_client_out = Output(UInt(log2Ceil(num_clients).W))
    val cache_in = Input(Vec(num_clients,UInt(2.W)))
    val cache_out = Output(Vec(num_clients,UInt(2.W)))
  })
  
  io.channel1_out:=io.channel1_in
  io.channel2_4_out:=io.channel2_4_in
  io.channel3_out:=io.channel3_in
  io.home_sharer_list_out:=io.home_sharer_list_in
  io.home_invalidate_list_out:=io.home_invalidate_list_in
  io.home_exclusive_granted_out:=io.home_exclusive_granted_in
  io.home_current_command_out:=io.home_current_command_in
  io.home_current_client_out:=io.home_current_client_in
  io.cache_out:=io.cache_in
  
}

class rule_client_requests_shared_access(cl:Int,num_clients:Int) extends rule(num_clients){


  when(io.en_r){
  
    when(((io.cache_in(cl) === e_invalid) && (io.channel1_in(cl) === e_empty))){
      io.channel1_out(cl) := e_req_shared
    }
  }
  
}

class rule_client_requests_exclusive_access(cl:Int,num_clients:Int) extends rule(num_clients){


  when(io.en_r){
  
    when((((io.cache_in(cl) === e_invalid) || (io.cache_in(cl) === e_shared)) && (io.channel1_in(cl) === e_empty))){
      io.channel1_out(cl) := e_req_exclusive
    }
  }
  
}

class rule_home_picks_new_request(cl:Int,num_clients:Int) extends rule(num_clients){


  when(io.en_r){
  
    when(((io.home_current_command_in === e_empty) && (io.channel1_in(cl) =/= e_empty))){
      io.home_current_command_out := io.channel1_out(cl)
      io.channel1_out(cl) := e_empty
      io.home_current_client_out := cl.U
      for( i <- 0 until num_clients){
        io.home_invalidate_list_out(i) := io.home_sharer_list_out(i)
      }
    }
  }
  
}

class rule_home_sends_invalidate_message(cl:Int,num_clients:Int) extends rule(num_clients){


  when(io.en_r){
  
    when(((((io.home_current_command_in === e_req_shared) && io.home_exclusive_granted_in) || (io.home_current_command_in === e_req_exclusive)) && (io.home_invalidate_list_in(cl) && (io.channel2_4_in(cl) === e_empty)))){
      io.channel2_4_out(cl) := e_invalidate
      io.home_invalidate_list_out(cl) := false.B
    }
  }
  
}

class rule_home_receives_invalidate_acknowledgement(cl:Int,num_clients:Int) extends rule(num_clients){


  when(io.en_r){
  
    when(((io.home_current_command_in =/= e_empty) && (io.channel3_in(cl) === e_invalidate_ack))){
      io.home_sharer_list_out(cl) := false.B
      io.home_exclusive_granted_out := false.B
      io.channel3_out(cl) := e_empty
    }
  }
  
}

class rule_sharer_invalidates_cache(cl:Int,num_clients:Int) extends rule(num_clients){


  when(io.en_r){
  
    when(((io.channel2_4_in(cl) === e_invalidate) && (io.channel3_in(cl) === e_empty))){
      io.channel2_4_out(cl) := e_empty
      io.channel3_out(cl) := e_invalidate_ack
      io.cache_out(cl) := e_invalid
    }
  }
  
}

class rule_client_receives_shared_grant(cl:Int,num_clients:Int) extends rule(num_clients){


  when(io.en_r){
  
    when((io.channel2_4_in(cl) === e_grant_shared)){
      io.cache_out(cl) := e_shared
      io.channel2_4_out(cl) := e_empty
    }
  }
  
}

class rule_client_received_exclusive_grant(cl:Int,num_clients:Int) extends rule(num_clients){


  when(io.en_r){
  
    when((io.channel2_4_in(cl) === e_grant_exclusive)){
      io.cache_out(cl) := e_exclusive
      io.channel2_4_out(cl) := e_empty
    }
  }
  
}

class rule_home_sends_reply_to_client_shared(num_clients:Int) extends rule(num_clients){


  when(io.en_r){
  
    when(((io.home_current_command_in === e_req_shared) && (!(io.home_exclusive_granted_in) && (io.channel2_4_in(home_current_client) === e_empty)))){
      io.home_sharer_list_out(home_current_client) := true.B
      io.home_current_command_out := e_empty
      io.channel2_4_out(home_current_client) := e_grant_shared
    }
  }
  
}

class rule_home_sends_reply_to_client_exclusive(num_clients:Int) extends rule(num_clients){


  when(io.en_r){
    var forall_flag = true.B
    for(i <- 0 until num_clients){
    when(!((io.home_sharer_list_in(i) === false.B))){
      forall_flag = false.B
    }
    }
    when(((io.home_current_command_in === e_req_exclusive) && (forall_flag && (io.channel2_4_in(home_current_client) === e_empty)))){
      io.home_sharer_list_out(home_current_client) := true.B
      io.home_current_command_out := e_empty
      io.channel2_4_out(home_current_client) := e_grant_exclusive
    }
  }
  
}

class cache_err(num_clients:Int) extends Module{

  val e_empty::e_req_shared::e_req_exclusive::e_invalidate::e_invalidate_ack::e_grant_shared::e_grant_exclusive::Nil = Enum(7)
  val e_invalid::e_shared::e_exclusive::Nil = Enum(3)

  val io = IO(new Bundle {
    val en_a = Input(UInt(log2Ceil(8*num_clients+2).W))
    val channel1_out = Output(Vec(num_clients,UInt(3.W)))
    val channel2_4_out = Output(Vec(num_clients,UInt(3.W)))
    val channel3_out = Output(Vec(num_clients,UInt(3.W)))
    val home_sharer_list_out = Output(Vec(num_clients,Bool()))
    val home_invalidate_list_out = Output(Vec(num_clients,Bool()))
    val home_exclusive_granted_out = Output(Bool()) 
    val home_current_command_out = Output(UInt(3.W))
    val home_current_client_out = Output(UInt(log2Ceil(num_clients).W))
    val cache_out = Output(Vec(num_clients,UInt(2.W)))
  })
  
  val channel1_init = Wire(Vec(num_clients,UInt(3.W)))
  val channel1_reg = RegInit(channel1_init)
  io.channel1_out:=channel1_reg
  val channel2_4_init = Wire(Vec(num_clients,UInt(3.W)))
  val channel2_4_reg = RegInit(channel2_4_init)
  io.channel2_4_out:=channel2_4_reg
  val channel3_init = Wire(Vec(num_clients,UInt(3.W)))
  val channel3_reg = RegInit(channel3_init)
  io.channel3_out:=channel3_reg
  val home_sharer_list_init = Wire(Vec(num_clients,Bool()))
  val home_sharer_list_reg = RegInit(home_sharer_list_init)
  io.home_sharer_list_out:=home_sharer_list_reg
  val home_invalidate_list_init = Wire(Vec(num_clients,Bool()))
  val home_invalidate_list_reg = RegInit(home_invalidate_list_init)
  io.home_invalidate_list_out:=home_invalidate_list_reg
  val home_exclusive_granted_init = Wire(Bool())
  val home_exclusive_granted_reg = RegInit(home_exclusive_granted_init)
  io.home_exclusive_granted_out:=home_exclusive_granted_reg
  val home_current_command_init = Wire(UInt(3.W))
  val home_current_command_reg = RegInit(home_current_command_init)
  io.home_current_command_out:=home_current_command_reg
  val home_current_client_init = Wire(UInt(log2Ceil(num_clients).W))
  val home_current_client_reg = RegInit(home_current_client_init)
  io.home_current_client_out:=home_current_client_reg
  val cache_init = Wire(Vec(num_clients,UInt(2.W)))
  val cache_reg = RegInit(cache_init)
  io.cache_out:=cache_reg
  
  for( i <- 0 until num_clients){
    channel1_init(i) := e_empty
    channel2_4_init(i) := e_empty
    channel3_init(i) := e_empty
    cache_init(i) := e_invalid
    home_sharer_list_init(i) := false.B
    home_invalidate_list_init(i) := false.B
  }
  home_current_command_init := e_empty
  home_exclusive_granted_init := false.B
  
  var rules = ArrayBuffer[rule]()
  var index = 0
  for(cl <- 0 until num_clients) {
    rules += Module(new rule_client_requests_shared_access(cl, num_clients))
    rules += Module(new rule_client_requests_exclusive_access(cl, num_clients))
    rules += Module(new rule_home_picks_new_request(cl, num_clients))
    rules += Module(new rule_home_sends_invalidate_message(cl, num_clients))
    rules += Module(new rule_home_receives_invalidate_acknowledgement(cl, num_clients))
    rules += Module(new rule_sharer_invalidates_cache(cl, num_clients))
    rules += Module(new rule_client_receives_shared_grant(cl, num_clients))
    rules += Module(new rule_client_received_exclusive_grant(cl, num_clients))
  }
  
  rules += Module(new rule_home_sends_reply_to_client_shared(num_clients))
  rules += Module(new rule_home_sends_reply_to_client_exclusive(num_clients))
  for(i <- 0 until 8*num_clients+2) {
    rules(i).io.channel1_in := channel1_reg
    rules(i).io.channel2_4_in := channel2_4_reg
    rules(i).io.channel3_in := channel3_reg
    rules(i).io.home_sharer_list_in := home_sharer_list_reg
    rules(i).io.home_invalidate_list_in := home_invalidate_list_reg
    rules(i).io.home_exclusive_granted_in := home_exclusive_granted_reg
    rules(i).io.home_current_command_in := home_current_command_reg
    rules(i).io.home_current_client_in := home_current_client_reg
    rules(i).io.cache_in := cache_reg
    rules(i).io.en_r:=(io.en_a=== index.U)
    when(io.en_a=== index.U){
      channel1_reg := rules(i).io.channel1_out 
      channel2_4_reg := rules(i).io.channel2_4_out 
      channel3_reg := rules(i).io.channel3_out 
      home_sharer_list_reg := rules(i).io.home_sharer_list_out 
      home_invalidate_list_reg := rules(i).io.home_invalidate_list_out 
      home_exclusive_granted_reg := rules(i).io.home_exclusive_granted_out 
      home_current_command_reg := rules(i).io.home_current_command_out 
      home_current_client_reg := rules(i).io.home_current_client_out 
      cache_reg := rules(i).io.cache_out 
    }
    index = index +1 
  }
  for (c1 <- 0 until num_clients){
    for (c2 <- 0 until num_clients){
  
      assert((!(((c1.U =/= c2.U) && (cache_reg(c1) === e_exclusive)))||((cache_reg(c2) === e_invalid))))
    }
  }
  
}

