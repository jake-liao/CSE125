/* 
 CSE125 Lab 3 direct_test.v
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

module direct_test;
	
	// Direct Inputs
	localparam [63:0] A = {32{2'b01}};
	localparam [63:0] B = {32{2'b10}};
	localparam [63:0] C = {16{4'b0011}};
	localparam [63:0] D = {16{4'b1100}};
	localparam WIDTH = 64;
	localparam DEPTH = 5;

	// input/ outputs
	reg clk, res_n, shift_in, shift_out;
	reg [WIDTH -1 :0] data_in;
	wire full, empty;
	wire [WIDTH -1:0] data_out;
    
    //File I/O variables
    integer i;    
    integer file_id;

	// input data
	reg [WIDTH -1:0] case_1 [0:4];

	// Instantiate FIFO
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
        //load input memory file contents
        $readmemb("data.mem", case_1);
        //opens file for writing
        
        file_id = $fopen("file_out3.txt", "w");
        
        
        
        
        // Initialize inputs and reset
        clk = 0;
        res_n = 0;
        shift_in = 0;
        shift_out = 0;
        @(posedge clk); @(posedge clk); res_n = 1;
        
        //First case
        for(i=0; i < DEPTH; i=i+1) begin
        	shift_in = 1'b1;
            data_in = case_1[i];
            
            #5 shift_in = 1'b0;
            @(posedge clk);
        end
        
        #10 shift_out = 1'b1;
        
        //outputs whenever a valid shift_out occurs
        $fdisplay(file_id, "%b", data_out);
        @(posedge clk);
        
        if(full && (data_out == case_1[0]))
            $display("FIFO is filled properly");
        else if(!full)
            $display("FIFO is not filled as expected");
        else
            $display("FIFO is not properly filled"); 
        #1 res_n = 0;
        @(posedge clk); #1 res_n = 1;
        
        $fclose(file_id);
        
    end
    
 
endmodule
