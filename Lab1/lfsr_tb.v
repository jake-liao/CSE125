`timescale 1ns / 1ps
//////////////////////////////////////////////////////////////////////////////////
// Company: 
// Engineer: 
// 
// Create Date: 03/31/2020 05:21:35 PM
// Design Name: 
// Module Name: lfsr_tb
// Project Name: 
// Target Devices: 
// Tool Versions: 
// Description: 
// 
// Dependencies: 
// 
// Revision:
// Revision 0.01 - File Created
// Additional Comments:
// 
//////////////////////////////////////////////////////////////////////////////////


module lfsr_tb;

    reg clk, res_n;
    reg [7:0] data_in;
    wire [7:0] data_out_struct;
    wire [7:0] data_out_behav;

    integer i, error_struct, error_behav;
    integer expected_lfsr [0:9];
    
    //Instantiate both models
    lfsr_structural dut_struct(.clk(clk),
                        .res_n(res_n),
                        .data_in(data_in),
                        .data_out(data_out_struct)
                        );


    lfsr_behavioural dut_behav(.clk(clk),
                        .res_n(res_n),
                        .data_in(data_in),                       
                        .data_out(data_out_behav)
                        );    

    // Set the clock to always toggle with a delay
    always begin
        #200 clk = !clk; 
    end
    
    initial begin
        //Array initialization is verbose in Verilog
        expected_lfsr[0] = 'h57;
        expected_lfsr[1] = 'hae;
        expected_lfsr[2] = 'h41;
        expected_lfsr[3] = 'h82;
        expected_lfsr[4] = 'h19;
        expected_lfsr[5] = 'h32;
        expected_lfsr[6] = 'h64;
        expected_lfsr[7] = 'hc8;
        expected_lfsr[8] = 'h8d;
        expected_lfsr[9] = 'h07;
        // Set initial clock, data, and reset
        clk = 'b0;
        res_n = 'b0;
        data_in = 'b1010_0101;

        @(posedge clk);
        @(negedge clk);
        // Stop the reset after at least 1 clock positive edge.
        res_n = 'b1;
        @(posedge clk);

        error_struct = 0; 
        error_behav = 0;
        // Wait 10 clock cycles and stop simulation
        for(i = 0; i < 10; i = i+1) begin
            @(posedge clk);
            // Check the output at all cycles
            if(data_out_struct != expected_lfsr[i]) begin
                $display("Error with structural LFSR output at cycle %d", i);
                error_struct = error_struct+1;
            end    

            if(data_out_behav != expected_lfsr[i]) begin
                $display("Error with behavioral LFSR output at cycle %d", i);
                error_behav = error_behav+1;
            end
        end    
            
        if(error_struct != 0)
            $display("Structural LFSR failed");
        else
            $display("Structural LFSR passed");
            
        if(error_behav != 0)
            $display("Behavioral LFSR failed");
        else
            $display("Behavioral LFSR passed");   
            
        $finish;
    end

endmodule
