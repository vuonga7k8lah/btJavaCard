# btJavaCard
chạy chương trình:
	/select 112233445566
	
	nhap diem:
		/send a001 [P1] [P2] [data_len] [data_input] [end_symbol_(optional)]
		/send a0010000080107020803060408AB
	in diem:
		/send a002 [P1] [P2] 
		/send a0020000
	sua diem:
		/send a003 [P1] [P2]	: [p1] = mon hoc; [p2] = diem thi
		/send a0030207
    xoa diem:
		/send a004 [P1] [P2] 	: [p1] = mon hoc
		/send a0040200