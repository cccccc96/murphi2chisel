; inductive invariant
(define-fun .induct_inv () Bool (! 
; size 25
(and
	(not (and (= _s2_Cache_reg_0_State (_ bv2 2)) (not (and (not _s56_$formal$German.sv_33898$1103_EN) _s63_id150))))
	(not (and (not (and (not _s56_$formal$German.sv_33898$1103_EN) _s63_id150)) (= _s4_Cache_reg_1_State (_ bv2 2))))
	(not (and (not (and (not _s56_$formal$German.sv_33898$1103_EN) _s63_id150)) (= _s21_Chan2_reg_0_Cmd (_ bv6 3))))
	(not (and (not (and (not _s56_$formal$German.sv_33898$1103_EN) _s63_id150)) (= _s23_Chan2_reg_1_Cmd (_ bv6 3))))
	(not (and (not (and (not _s56_$formal$German.sv_33898$1103_EN) _s63_id150)) (= _s41_CurCmd_reg (_ bv4 3))))
	(not (and (not (and (not _s56_$formal$German.sv_33898$1103_EN) _s63_id150)) _s0_AuxData_reg))
	(not (and (not (and (not _s56_$formal$German.sv_33898$1103_EN) _s63_id150)) (= _s6_Cache_reg_2_State (_ bv2 2))))
	(not (and (not (and (not _s56_$formal$German.sv_33898$1103_EN) _s63_id150)) (= _s8_Cache_reg_3_State (_ bv2 2))))
	(not (and (not (and (not _s56_$formal$German.sv_33898$1103_EN) _s63_id150)) (= _s10_Cache_reg_4_State (_ bv2 2))))
	(not (and (not (and (not _s56_$formal$German.sv_33898$1103_EN) _s63_id150)) (= _s25_Chan2_reg_2_Cmd (_ bv6 3))))
	(not (and (not (and (not _s56_$formal$German.sv_33898$1103_EN) _s63_id150)) (= _s27_Chan2_reg_3_Cmd (_ bv6 3))))
	(not (and (not (and (not _s56_$formal$German.sv_33898$1103_EN) _s63_id150)) (= _s29_Chan2_reg_4_Cmd (_ bv6 3))))
	(not (and (not (and (not _s56_$formal$German.sv_33898$1103_EN) _s63_id150)) _s1_Cache_reg_0_Data))
	(not (and (not (and (not _s56_$formal$German.sv_33898$1103_EN) _s63_id150)) _s3_Cache_reg_1_Data))
	(not (and (not (and (not _s56_$formal$German.sv_33898$1103_EN) _s63_id150)) (and _s22_Chan2_reg_0_Data (= _s21_Chan2_reg_0_Cmd (_ bv5 3)))))
	(not (and (not (and (not _s56_$formal$German.sv_33898$1103_EN) _s63_id150)) (and _s24_Chan2_reg_1_Data (= _s23_Chan2_reg_1_Cmd (_ bv5 3)))))
	(not (and (not (and (not _s56_$formal$German.sv_33898$1103_EN) _s63_id150)) _s49_MemData_reg))
	(not (and (not (and (not _s56_$formal$German.sv_33898$1103_EN) _s63_id150)) (and (not (= _s2_Cache_reg_0_State (_ bv1 2))) (not (= _s2_Cache_reg_0_State (_ bv0 2))))))
	(not (and (not (and (not _s56_$formal$German.sv_33898$1103_EN) _s63_id150)) (and (not (= _s4_Cache_reg_1_State (_ bv1 2))) (not (= _s4_Cache_reg_1_State (_ bv0 2))))))
	(not (and (not (and (not _s56_$formal$German.sv_33898$1103_EN) _s63_id150)) _s32_Chan3_reg_0_Data))
	(not (and (not (and (not _s56_$formal$German.sv_33898$1103_EN) _s63_id150)) _s34_Chan3_reg_1_Data))
	(not (and (not (and (not _s56_$formal$German.sv_33898$1103_EN) _s63_id150)) _s36_Chan3_reg_2_Data))
	(not (and (not (and (not _s56_$formal$German.sv_33898$1103_EN) _s63_id150)) _s38_Chan3_reg_3_Data))
	(not (and (not (and (not _s56_$formal$German.sv_33898$1103_EN) _s63_id150)) _s40_Chan3_reg_4_Data))
	property
)
 :invar-property 1))

; inductive invariant next
(define-fun .induct_inv_next () Bool 
; size 25
(and
	(not (and (= _s2_Cache_reg_0_State$next (_ bv2 2)) (not (and (not _s56_$formal$German.sv_33898$1103_EN$next) _s63_id150$next))))
	(not (and (not (and (not _s56_$formal$German.sv_33898$1103_EN$next) _s63_id150$next)) (= _s4_Cache_reg_1_State$next (_ bv2 2))))
	(not (and (not (and (not _s56_$formal$German.sv_33898$1103_EN$next) _s63_id150$next)) (= _s21_Chan2_reg_0_Cmd$next (_ bv6 3))))
	(not (and (not (and (not _s56_$formal$German.sv_33898$1103_EN$next) _s63_id150$next)) (= _s23_Chan2_reg_1_Cmd$next (_ bv6 3))))
	(not (and (not (and (not _s56_$formal$German.sv_33898$1103_EN$next) _s63_id150$next)) (= _s41_CurCmd_reg$next (_ bv4 3))))
	(not (and (not (and (not _s56_$formal$German.sv_33898$1103_EN$next) _s63_id150$next)) _s0_AuxData_reg$next))
	(not (and (not (and (not _s56_$formal$German.sv_33898$1103_EN$next) _s63_id150$next)) (= _s6_Cache_reg_2_State$next (_ bv2 2))))
	(not (and (not (and (not _s56_$formal$German.sv_33898$1103_EN$next) _s63_id150$next)) (= _s8_Cache_reg_3_State$next (_ bv2 2))))
	(not (and (not (and (not _s56_$formal$German.sv_33898$1103_EN$next) _s63_id150$next)) (= _s10_Cache_reg_4_State$next (_ bv2 2))))
	(not (and (not (and (not _s56_$formal$German.sv_33898$1103_EN$next) _s63_id150$next)) (= _s25_Chan2_reg_2_Cmd$next (_ bv6 3))))
	(not (and (not (and (not _s56_$formal$German.sv_33898$1103_EN$next) _s63_id150$next)) (= _s27_Chan2_reg_3_Cmd$next (_ bv6 3))))
	(not (and (not (and (not _s56_$formal$German.sv_33898$1103_EN$next) _s63_id150$next)) (= _s29_Chan2_reg_4_Cmd$next (_ bv6 3))))
	(not (and (not (and (not _s56_$formal$German.sv_33898$1103_EN$next) _s63_id150$next)) _s1_Cache_reg_0_Data$next))
	(not (and (not (and (not _s56_$formal$German.sv_33898$1103_EN$next) _s63_id150$next)) _s3_Cache_reg_1_Data$next))
	(not (and (not (and (not _s56_$formal$German.sv_33898$1103_EN$next) _s63_id150$next)) (and _s22_Chan2_reg_0_Data$next (= _s21_Chan2_reg_0_Cmd$next (_ bv5 3)))))
	(not (and (not (and (not _s56_$formal$German.sv_33898$1103_EN$next) _s63_id150$next)) (and _s24_Chan2_reg_1_Data$next (= _s23_Chan2_reg_1_Cmd$next (_ bv5 3)))))
	(not (and (not (and (not _s56_$formal$German.sv_33898$1103_EN$next) _s63_id150$next)) _s49_MemData_reg$next))
	(not (and (not (and (not _s56_$formal$German.sv_33898$1103_EN$next) _s63_id150$next)) (and (not (= _s2_Cache_reg_0_State$next (_ bv1 2))) (not (= _s2_Cache_reg_0_State$next (_ bv0 2))))))
	(not (and (not (and (not _s56_$formal$German.sv_33898$1103_EN$next) _s63_id150$next)) (and (not (= _s4_Cache_reg_1_State$next (_ bv1 2))) (not (= _s4_Cache_reg_1_State$next (_ bv0 2))))))
	(not (and (not (and (not _s56_$formal$German.sv_33898$1103_EN$next) _s63_id150$next)) _s32_Chan3_reg_0_Data$next))
	(not (and (not (and (not _s56_$formal$German.sv_33898$1103_EN$next) _s63_id150$next)) _s34_Chan3_reg_1_Data$next))
	(not (and (not (and (not _s56_$formal$German.sv_33898$1103_EN$next) _s63_id150$next)) _s36_Chan3_reg_2_Data$next))
	(not (and (not (and (not _s56_$formal$German.sv_33898$1103_EN$next) _s63_id150$next)) _s38_Chan3_reg_3_Data$next))
	(not (and (not (and (not _s56_$formal$German.sv_33898$1103_EN$next) _s63_id150$next)) _s40_Chan3_reg_4_Data$next))
	property$next
)
)

(echo "asserting design: transition relation and global constraints")
(assert .trans)

(push 1)
(echo "checking (initial -> proof)")
(assert (and .init (not .induct_inv)))
(check-sat)
(pop 1)

(push 1)
(echo "checking (proof -> property)")
(assert (and .induct_inv (not property)))
(check-sat)
(pop 1)

(push 1)
(echo "checking (proof is inductive)")
(assert (and .induct_inv (not .induct_inv_next)))
(check-sat)
(pop 1)


