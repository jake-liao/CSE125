`timescale 1ns / 1ps
//////////////////////////////////////////////////////////////////////////////////
// Company: 
// Engineer: 
// 
// Create Date: 04/08/2020 06:56:39 PM
// Design Name: 
// Module Name: multiplier_1
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


module multiplier_1(
    input clk,
    input res_n,
    input start,
    output reg done,
    input [15:0] arg1,
    input [15:0] arg2,
    output reg [31:0] product
    );
    integer i;
    always @(posedge start)begin
        done = 1'b0;
        i = arg1 + 1;
    end
    always @(posedge clk)begin
        i = i - 1;
        if(i>0)begin
            product = product + arg2;
            
        end

        else if(i==0)begin
            done = 1'b1;
        end
    end
    always @(negedge res_n)begin
        i = 1'b0;
        done = 1'b0;
        product = 1'b0;
    end
    
endmodule
