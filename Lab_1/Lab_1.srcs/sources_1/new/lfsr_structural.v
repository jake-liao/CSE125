/* 
 CSE125 Lab 1 lfsr_structural.v
 Professor Heiner Litz
 Jake Liao
 Mark Zarkharov

 FF set to data_in when res_n is 0 regardless of clock state
  CLK  |  res_n  |  data_in   |  output
 ----------------------------------------
   0   |    0    |  data_in   |  data_in
   0   |    1    |  data_in   | no change
   1   |    0    |  data_in   |  data_in
   1   |    1    |  data_in   |   shift
*/
`timescale 1ns / 1ps
module lfsr_structural(
    input clk,
    input res_n,
    input [7:0] data_in,
    output [7:0] data_out
    );
    
    wire [7:0] d_shift; // bus for the next input when not resetting
    wire [7:0] flop_in; // output of the mux
    
    // data_out -> shift bus -> mux
    assign d_shift[0] = data_out[7];
    assign d_shift[1] = data_out[0];
    assign d_shift[2] = data_out[1] ^ data_out[7];
    assign d_shift[3] = data_out[2] ^ data_out[7];
    assign d_shift[4] = data_out[3] ^ data_out[7];
    assign d_shift[5] = data_out[4];
    assign d_shift[6] = data_out[5];    
    assign d_shift[7] = data_out[6];  
    
    // d_shift when mux selector is 1, data_in when mux selector is 0
    busmux_8b_2_1 EightBitMux(.x(data_in[7:0]), .y(d_shift[7:0]), .sel(res_n), .f(flop_in[7:0]));
    
    // Flip Flop: reset is not used, CE is set to clk, Q initialized to 0
    FDCE #(.INIT(1'b0)) FF0 (.C(clk), .CLR(1'b0), .CE(1'b1), .D(flop_in[0]), .Q(data_out[0]));
    FDCE #(.INIT(1'b0)) FF1 (.C(clk), .CLR(1'b0), .CE(1'b1), .D(flop_in[1]), .Q(data_out[1]));
    FDCE #(.INIT(1'b0)) FF2 (.C(clk), .CLR(1'b0), .CE(1'b1), .D(flop_in[2]), .Q(data_out[2]));
    FDCE #(.INIT(1'b0)) FF3 (.C(clk), .CLR(1'b0), .CE(1'b1), .D(flop_in[3]), .Q(data_out[3]));
    FDCE #(.INIT(1'b0)) FF4 (.C(clk), .CLR(1'b0), .CE(1'b1), .D(flop_in[4]), .Q(data_out[4]));
    FDCE #(.INIT(1'b0)) FF5 (.C(clk), .CLR(1'b0), .CE(1'b1), .D(flop_in[5]), .Q(data_out[5]));
    FDCE #(.INIT(1'b0)) FF6 (.C(clk), .CLR(1'b0), .CE(1'b1), .D(flop_in[6]), .Q(data_out[6]));
    FDCE #(.INIT(1'b0)) FF7 (.C(clk), .CLR(1'b0), .CE(1'b1), .D(flop_in[7]), .Q(data_out[7]));
endmodule

// 8 bit bus multiplexor
// y on sel high, x on sel low
module busmux_8b_2_1(
    input [7:0] x,
    input [7:0] y,
    input sel,
    output [7:0] f
    );
    
    assign f[0] = x[0] & ~sel | y[0] & sel;
    assign f[1] = x[1] & ~sel | y[1] & sel;
    assign f[2] = x[2] & ~sel | y[2] & sel;
    assign f[3] = x[3] & ~sel | y[3] & sel;
    assign f[4] = x[4] & ~sel | y[4] & sel;
    assign f[5] = x[5] & ~sel | y[5] & sel;
    assign f[6] = x[6] & ~sel | y[6] & sel;
    assign f[7] = x[7] & ~sel | y[7] & sel;
endmodule