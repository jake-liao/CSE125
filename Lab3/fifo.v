/* 
 CSE125 Lab 3 fifo.v
 Professor Heiner Litz
 Jake Liao
 Mark Zakharov
*/
`timescale 1ns / 1ps
module fifo #(parameter WIDTH=64, parameter DEPTH=8)(input clk, res_n, shift_in, shift_out,
                                                    input [WIDTH-1:0] data_in,
                                                    output reg full, empty,
                                                    output [WIDTH-1:0] data_out);
                                                    
    //creates bus to link stages full and data signals together
    reg [WIDTH-1:0] in_bus [DEPTH-1:0]; 
    reg  s_in_bus [DEPTH-1:0];
    reg  s_out_bus [DEPTH-1:0];
    reg next_full, next_empty;
    wire full_bus [DEPTH-1:0];
    wire [WIDTH-1:0] out_bus [DEPTH-1:0]; 
    integer count = 0, next_count = 0;
    integer j;  

    assign data_out = out_bus[DEPTH - 1];

    genvar i;
    generate
        for(i=0; i<DEPTH; i=i+1) begin: STA
            if(i==0) begin  //first instance, takes data_in as 0, though that data should not be shifted in
                 stage #(.STAGEWIDTH(WIDTH)) TAIL (.clk(clk),
                                                .shift_in(s_in_bus[i]),
                                                .shift_out(s_out_bus[i]),
                                                .fifo_data(in_bus[i]),
                                                .prev_stage_data({WIDTH{1'b0}}),
                                                .stage_data(out_bus[i])); end
            else if(i<DEPTH-1) begin //middle instances
                stage # (.STAGEWIDTH(WIDTH)) Mid (.clk(clk),
                                                .shift_in(s_in_bus[i]),
                                                .shift_out(s_out_bus[i]),
                                                .fifo_data(in_bus[i]),
                                                .prev_stage_data(out_bus[i-1]),
                                                .stage_data(out_bus[i])); end
            else begin    //last instance
                 stage # (.STAGEWIDTH(WIDTH)) HEAD (.clk(clk),
                                                .shift_in(s_in_bus[i]),
                                                .shift_out(s_out_bus[i]),
                                                .fifo_data(in_bus[i]),
                                                .prev_stage_data(out_bus[i-1]),
                                                .stage_data(out_bus[i])); end
        end
    endgenerate
    
    always @(*) begin
        if (count == DEPTH) begin
            next_full = 1'b1; 
            next_empty = 1'b0;
        end
        else if (count == 0) begin
            next_full = 1'b0; 
            next_empty = 1'b1;
        end
        else begin
            next_full = 1'b0;
            next_empty = 1'b0;
        end
    end

    always @(posedge clk) begin
        full <= next_full;
        empty <= next_empty;
        case ({shift_in, shift_out, res_n})
            default: count <= 1'b0;
            3'bxx0: count <= 1'b0;
            3'b001:
                begin
                    for (j = 0; j < DEPTH; j = j+ 1) begin
                        in_bus[j] <= {WIDTH{1'b0}}; 
                        s_in_bus[j] <= 1'b0;
                        s_out_bus[j] <= 1'b0;
                    end
                    count <= count;
                end
            3'b011:
                begin
                    for (j = 0; j < DEPTH; j = j+ 1) begin
                        in_bus[j] <= {WIDTH{1'b0}}; 
                        s_in_bus[j] <= 1'b0;
                        if (j >= DEPTH - count) begin
                            s_out_bus[j] <= 1'b1;
                        end
                    end
                    count <= count - 1; 
                end
            3'b101: 
                begin
                    for (j = 0; j < DEPTH; j = j+ 1) begin
                        if(j == (DEPTH-count-1)) begin 
                            in_bus[j]<=data_in; 
                            s_in_bus[j] <= 1'b1; 
                            s_out_bus[j] <= 1'b0;end
                        else begin in_bus[j] <= {WIDTH{1'b0}}; 
                            s_in_bus[j] <= 1'b0; 
                            s_out_bus[j] <= 1'b0;
                        end
                    end
                    count <= count + 1; 
                end
            3'b111:
                begin
                    for (j = 0; j < DEPTH; j = j+ 1) begin
                        if(j == (DEPTH-count)) begin 
                            in_bus[j]<=data_in;
                            s_in_bus[j] <= 1'b1; 
                            s_out_bus[j] <= 1'b1;end
                        else if (j > DEPTH - count) begin
                            s_out_bus[j] <= 1'b1;end
                        else begin 
                            in_bus[j] <= {WIDTH{1'b0}}; 
                            s_in_bus[j] <= 1'b0; 
                        end
                    end
                    count <= count; 
                end
        endcase
    end
    
endmodule

module stage #(parameter STAGEWIDTH=64)(input clk, shift_in, shift_out,
                                        input [STAGEWIDTH-1:0] fifo_data, prev_stage_data,
                                        output [STAGEWIDTH-1:0] stage_data);    
    
    reg next_full;
    reg [1:0] sel, next_sel;
    reg [STAGEWIDTH-1:0] register_out;
    wire [STAGEWIDTH-1:0] register_in;
    
    // MACROS
    localparam [1:0] HOLD = 2'b11; // 3
    localparam [1:0] SO = 2'b10; // 2
    localparam [1:0] SI = 2'b01; // 1
    localparam [1:0] ERR = 2'b00; // 0
    
    // MULTIPLEXOR
    MUX #(.WIDTH(STAGEWIDTH)) multiplexor_0 (.HOLD_IN(register_out),
                                             .SI_IN(fifo_data),
                                             .SO_IN(prev_stage_data),
                                             .SEL(sel),
                                             .OUT(register_in) );
    
    always @(*) begin
        // MUX SELECTOR COMBINATORY LOGIC
        casex({shift_in, shift_out})
            default:  sel = HOLD;  
            2'b00:  sel = HOLD;  
            2'b01:  sel = SO;  
            2'b10:  sel = SI;  
            2'b11:  sel = SI;  
        endcase

    end

    //clock registered values for full and output
    always @(posedge clk) begin
        register_out <= register_in;
    end
    assign stage_data = register_out;
endmodule

module MUX #(parameter WIDTH=64) ( input [WIDTH-1:0] HOLD_IN,
                                        input [WIDTH-1:0] SI_IN,
                                        input [WIDTH-1:0] SO_IN,
                                        input [1:0] SEL,               
                                        output reg [WIDTH-1:0] OUT);
    localparam [1:0] HOLD = 2'b11;
    localparam [1:0] SI = 2'b01;
    localparam [1:0] SO = 2'b10;
    localparam [1:0] DEFT = 2'b00;
    
    always @ (*) begin
        case (SEL)
            default: OUT = SI_IN;
            HOLD: OUT = HOLD_IN;
            SI: OUT = SI_IN;
            SO: OUT = SO_IN;
            DEFT: OUT = SI_IN;
        endcase
    end
    
endmodule

