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
    integer i;  //doubles as counter and enable for the accumulator
    always @(posedge start)begin
        done = 1'b0;
        i = arg1;
    end
    always @(posedge clk)begin  //values are updated each clock cycle if i > 0
        if(i>1)begin
            product = product + arg2;   //basic operation, adds arg2 (arg1) times
            i = i - 1;   
        end
        else if(i==1)begin
            product = product + arg2;
            i = i - 1;  //final decrement of i, disabling the functions of this black
            done = 1'b1; //sets done high knowing this is the final accumulator operation
        end
    end
    always @(negedge res_n)begin    //resets all output signals and i to disable accumulation
        i = 1'b0;
        done = 1'b0;
        product = 1'b0;
    end
endmodule
