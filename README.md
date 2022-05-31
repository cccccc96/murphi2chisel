# protocolVerify

Read a protocol modeled in Murphi and translate it into chisel. Then we turn chisel into verilog/netlist(btor) , and finally perform model checking with avr.

### Build from source 

* Download/clone the GitHub repository
* simply run ./build.sh from the protocolVerify folder to install all dependencies and compile the source code.

### How to run

```
python3 <path>/<file>.m -o <output-path> -c <const-list>
(verify the <path>/<file>.m ; the generated chisel/verilog/btor2 and the verification result will in <output-path>)

Example:   python3 verify.py ./example/mutual.m -c 2 -o out 
```

