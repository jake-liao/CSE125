/* 
 CSE125 Lab 3 random_test.v
 Professor Heiner Litz
 Jake Liao
 Mark Zakharov
---------------------------------
A. NORMAL CASES:
 		1. when full
 		2. when empty
 		3. when neither
B. CORNER CASES:
 		4. shift_out when empty
 		5. shift_in when full?
*/
`timescale 1ns / 1ps
module random_test;

	localparam WIDTH = 64;
	localparam DEPTH = 8;
	localparam TEST_NUMBER = 50;

	// input/ outputs
	reg clk, res_n, shift_in, shift_out;
	reg [WIDTH -1 :0] data_in;
	wire full, empty;
	wire [WIDTH -1:0] data_out;
    
    //File I/O variables
    integer in, out, count, rand, o_flag;    
    
    //seed for rng
    integer seed = 9;

	//randomized input data
	reg [WIDTH -1:0] vec[TEST_NUMBER-1:0];
	//gets loaded with random output data for comparison with random input data
	reg [WIDTH-1:0] compare_vec[TEST_NUMBER-1:0];
	
	//module initialization
	fifo #(.WIDTH(WIDTH), .DEPTH(DEPTH)) FIFO_0 (.clk(clk),
                                            .res_n(res_n),
                                            .shift_in(shift_in),
                                            .shift_out(shift_out),
                                            .data_in (data_in),
                                            .full(full),
                                            .empty(empty),
                                            .data_out(data_out));
    always 
    #5 clk = !clk;
    
    initial begin
        //initialize starting values
        clk = 0;
        res_n = 1;
        shift_in = 0;
        shift_out = 0;
        data_in = 0;
        
        //loads in effective addresses of generated text files to hold random data and compare output
        in = $fopen("random_input_data.txt","w");
        out = $fopen("random_output_data.txt", "w");
        
        //generates random data and writes to file
        for (count=0; count<TEST_NUMBER; count=count+1) begin
            rand=$random(seed);
            $fwrite(in, "%b\n",rand);
        end
        
        //done with writing random values
        $fclose(in);
        
        //reads random data out of file into vector used as fifo data input
        $readmemb("random_input_data.txt",vec);
            
        #20
        //o_flag will be used to send output to file clock cycle after shift out signal goes high
        o_flag = 0;
        count = 0;
        
        //randomly generates TEST_NUMBER shift_in and shift_out signals, passing in previously generated data
        while(count<TEST_NUMBER || (~empty)) begin
            
            @(posedge clk);
            #10;
            //logs output data after clock cycle, only once after each shift_out signal 
            if(o_flag==1) begin
                $fwrite(out, "%b\n", data_out);
                o_flag = 0;
            end
            //checks to see if shift_in is allowed, ensures only TEST_NUMBER amount of new values inputted
            if(~full && (count<TEST_NUMBER)) begin  
                //randomly sets shift_in   
                if((($random(seed)%2)==1)  || (count<=7)) begin
                    data_in = vec[count];
                    shift_in = 1'b1;
                    //Will shift in as many times as there are random values
                    count=count+1;
                end
                //randomly clears shift_in
                else begin
                    shift_in = 1'b0;
                end
            end
            //prevents shift_in from going high if full
            else begin
                shift_in = 1'b0;
            end
            //checks is empty is high and if the currnt data_out is empty
            if(~empty && ((data_out[0] === 1) || (data_out[0] === 0))) begin
                //randomly sets shift_out
                if((($random(seed)%2)==1) && (count>7))begin
                    shift_out = 1'b1;
                    o_flag=1;
                    
                end
                //randomly clears shift_out
                else begin
                    shift_out = 1'b0;
                end
            end
            //prevents shift_out from going high if empty
            else begin
                shift_out = 1'b0;
            end
            
            
        end
        shift_in = 1'b0;
        shift_out = 1'b0;
        

        //loads outputted data into vector for comparison with original input data found in vec
        $readmemb("random_output_data.txt",compare_vec);
        $fclose(out);
        #10
        //resets count to compare vectors of each in/out value in order
        count = 0;
        
        while(count<TEST_NUMBER) begin
            if(compare_vec[count] != vec[count]) begin
                $display("Random input number does not match output");
            end
            count=count+1;
        end
    end
endmodule