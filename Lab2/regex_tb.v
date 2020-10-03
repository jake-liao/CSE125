`timescale 1ns / 1ps

module regex_tb;
    // Params to simplify test case initialization
    localparam [1:0] A = 2'b00;
    localparam [1:0] B = 2'b01;
    localparam [1:0] C = 2'b10;
    localparam [1:0] D = 2'b11;


    reg clk, res_n, last_symbol;
    reg [1:0] symbol_in;
    
    wire result, done;

    // Test Cases: ABCDD, ABBBCAABD, ABCDDD, ABBCAAD
    integer case_1_len = 5;
    integer case_2_len = 9;
    integer case_3_len = 6;
    integer case_4_len = 7;
    reg [1:0] case_1 [0:4];
    reg [1:0] case_2 [0:8];
    reg [1:0] case_3 [0:5];
    reg [1:0] case_4 [0:6];
    reg [3:0] expected_results = {1'b0,1'b1,1'b0,1'b0};
  
    // Instantiate the regex
    regex dut(.clk(clk), 
              .res_n(res_n), 
              .symbol_in(symbol_in),
              .last_symbol(last_symbol), 
              .result(result), 
              .done(done));
    
    always 
        #5 clk = !clk;
        
    integer i;    
        
    initial begin
        // Initialize the test cases
        case_1[0] = A; case_1[1] = B; case_1[2] = C; 
        case_1[3] = D; case_1[4] = D;
        
        case_2[0] = A; case_2[1] = B; case_2[2] = B; 
        case_2[3] = B; case_2[4] = C; case_2[5] = A;
        case_2[6] = A; case_2[7] = B; case_2[8] = D;
        
        case_3[0] = A; case_3[1] = B; case_3[2] = C; 
        case_3[3] = D; case_3[4] = D; case_3[5] = D;
        
        case_4[0] = A; case_4[1] = B; case_4[2] = B; 
        case_4[3] = C; case_4[4] = A; case_4[5] = A;
        case_4[6] = D;
    
        // Initialize inputs and reset
        clk = 0;
        res_n = 0;
        last_symbol = 0;
        @(posedge clk); @(posedge clk); #1 res_n = 1;
        
        //First case
        for(i=0; i < case_1_len; i=i+1) begin
            symbol_in = case_1[i];
            if(i == case_1_len-1)
                #1 last_symbol = 1'b1;
            @(posedge clk);
        end
        #1 last_symbol = 1'b0;
        @(posedge clk);
        if(done && (result == expected_results[0]))
            $display("Regex correctly identified first case.");
        else if(!done)
            $display("Regex did not signal done at expected timing.");
        else
            $display("Regex failed to correctly identify first case"); 
        #1 res_n = 0;
        @(posedge clk); #1 res_n = 1;
        
        //Second case
        for(i=0; i < case_2_len; i=i+1) begin
            symbol_in = case_2[i];
            if(i == case_2_len-1)
                #1 last_symbol = 1'b1;
            @(posedge clk);
        end
        #1 last_symbol = 1'b0;
        @(posedge clk);
        if(done && (result == expected_results[1]))
            $display("Regex correctly identified second case.");
        else if(!done)
            $display("Regex did not signal done at expected timing.");
        else
            $display("Regex failed to correctly identify second case"); 
        #1 res_n = 0;
        @(posedge clk); #1 res_n = 1;
            
        //Third case
        for(i=0; i < case_3_len; i=i+1) begin
            symbol_in = case_3[i];
            if(i == case_3_len-1)
                #1 last_symbol = 1'b1;
            @(posedge clk);
        end
        #1 last_symbol = 1'b0;
        @(posedge clk);
        if(done && (result == expected_results[2]))
            $display("Regex correctly identified third case.");
        else if(!done)
            $display("Regex did not signal done at expected timing.");
        else
            $display("Regex failed to correctly identify third case"); 
        #1 res_n = 0;
        @(posedge clk); #1 res_n = 1;
        
        //Fourth case
        for(i=0; i < case_4_len; i=i+1) begin
            symbol_in = case_4[i];
            if(i == case_4_len-1)
                #1 last_symbol = 1'b1;
            @(posedge clk);
        end
        #1 last_symbol = 1'b0;
        @(posedge clk);
        if(done && (result == expected_results[3]))
            $display("Regex correctly identified fourth case.");
        else if(!done)
            $display("Regex did not signal done at expected timing.");
        else
            $display("Regex failed to correctly identify fourth case"); 
        
    end



endmodule
