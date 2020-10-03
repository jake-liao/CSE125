`timescale 1ns / 1ps
//////////////////////////////////////////////////////////////////////////////////
// Company: 
// Engineer: 
// 
// Create Date: 04/03/2020 08:33:58 PM
// Design Name: 
// Module Name: multiplier_tb
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


module multiplier_tb;

    reg clk, res_n, start;
    reg [15:0] arg1, arg2;

    wire done_m1, done_m4;
    wire [31:0] product_m1, product_m4;

    integer expected_product = 67*43;

    // Instantiate both modules
    multiplier_1 dut_m1(.clk(clk), 
                        .res_n(res_n), 
                        .start(start), 
                        .done(done_m1),
                        .arg1(arg1), 
                        .arg2(arg2),
                        .product(product_m1));
                        
    multiplier_4 dut_m4(.clk(clk), 
                        .res_n(res_n), 
                        .start(start), 
                        .done(done_m4),
                        .arg1(arg1), 
                        .arg2(arg2),
                        .product(product_m4));                        
                        
    // Set the clock with an arbitrary delay                    
    always 
        #5 clk = !clk;

    initial begin
        // Initialize clock and hold reset for 1 clock posedge
        clk = 'b0;
        res_n= 'b0;          
        @(posedge clk);
        @(negedge clk) res_n = 'b1;
        
        // Set inputs and start
        arg1 = 16'd67;
        arg2 = 16'd43;
        start = 'b1;
        
        @(posedge clk);
        @(negedge clk) start = 'b0;
        
        // End simulation once the slower multiplier is done
        @(posedge done_m1);
        @(posedge clk);
        
        // Check for mult1
        if(product_m1 != expected_product)
            $display("Error with mult1 output");
        else
            $display("mult1 output is correct");
            
        // Check for mult4
        if(product_m4 != expected_product)
            $display("Error with mult4 output");
        else
            $display("mult4 output is correct");
                
        $finish; 
         
    end                             

endmodule
