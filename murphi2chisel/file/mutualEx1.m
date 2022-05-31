
const

  NODE_NUM : 2;

type

  NODE : scalarset(NODE_NUM);

  OTHER : enum {Other};

  ABS_NODE : union {NODE, OTHER};

  CONTROL : enum {I, T, C, E};

  STATE : record
  -- Program variables:
    n : array [NODE] of CONTROL;
  -- Auxiliary variables:
    x : boolean;
  end;

var

  Sta : STATE;

-------------------------------------------------------------------------------

startstate "Init"
  undefine Sta;
  for i : NODE do
    Sta.n[i] := I;
  end;
  Sta.x := true;
endstartstate;

-------------------------------------------------------------------------------

ruleset i : NODE do
rule "Try"
  Sta.n[i] = I
==>
begin
  Sta.n[i] := T;
endrule;
endruleset;

ruleset i : NODE do
rule "Crit"
  Sta.n[i] = T & Sta.x = true
==>
begin
  Sta.n[i] := C;
  Sta.x := false;
endrule;
endruleset;

ruleset i : NODE do
rule "Exit"
  Sta.n[i] = C
==>
begin
  Sta.n[i] := E;
endrule;
endruleset;

ruleset i : NODE do
rule "Idle"
  Sta.n[i] = E
==>
begin
  Sta.n[i] := I;
  Sta.x := true;
endrule;
endruleset;

-------------------------------------------------------------------------------

invariant "MutualExclusion"
  forall i : NODE do forall j : NODE do
    i != j -> !(Sta.n[i] = E & Sta.n[j] = E)
  end end;

-------------------------------------------------------------------------------

rule "ABS_Crit"
  Sta.x = true
==>
begin
  Sta.x := false;
endrule;

rule "ABS_Idle"
  forall j : NODE do
    Sta.n[j] != C & Sta.n[j] != E
  end                                -- by Lemma_1
==>
begin
  Sta.x := true;
endrule;

-------------------------------------------------------------------------------

invariant "Lemma_1"
  forall p : NODE do forall j : NODE do
    Sta.n[j] = E ->
    forall i : NODE do i != j -> Sta.n[i] != C & Sta.n[i] != E end
  end end;
