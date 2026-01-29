import java.time.LocalDateTime;

void main() throws Exception {    
    println(this.getClass().getName());
    while (true) {
        try {
            printInfo();
        } catch (Exception e) {
            System.out.println("Had exception" + e.getMessage());
            e.printStackTrace();
        }
        Thread.sleep(1000);
    }
}

void printInfo() {
    println("Generated %d at %s".formatted(randomNumber(), getDateTime()));
}

int randomNumber() {
    return (int) (Math.random() * 1000);
}

String getDateTime() {
    return LocalDateTime.now().toString();
}